/*
 * Copyright 2020-2026 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine

import com.coyotebitcoin.tatooine.config.SyncType
import com.coyotebitcoin.tatooine.sync.Kyoto
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.bitcoindevkit.Address
import org.bitcoindevkit.Amount
import org.bitcoindevkit.Descriptor
import org.bitcoindevkit.ElectrumClient
import org.bitcoindevkit.FeeRate
import org.bitcoindevkit.KeychainKind
import org.bitcoindevkit.Network
import org.bitcoindevkit.NetworkKind
import org.bitcoindevkit.Persister
import org.bitcoindevkit.Psbt
import org.bitcoindevkit.Script
import org.bitcoindevkit.SyncScriptInspector
import org.bitcoindevkit.Transaction
import org.bitcoindevkit.TxBuilder
import org.bitcoindevkit.UnconfirmedTx
import org.bitcoindevkit.Wallet as BdkWallet

@Serializable data class MonthlyStats(val month: String, val count: Int)

@Serializable
data class FaucetReport(
    val versionName: String,
    val balanceSats: ULong,
    val last7Days: Int,
    val last30Days: Int,
    val months: List<MonthlyStats>,
) {
    override fun toString(): String =
        "version=$versionName, balance=$balanceSats sats, last7Days=$last7Days, last30Days=$last30Days, months=$months"
}

class FaucetWallet(
    descriptorString: String,
    private val network: Network,
    electrumUrl: String,
    val syncType: SyncType,
    private val faucetAmount: ULong,
    private val versionName: String,
    dbFilePath: String,
) {
    private val wallet: BdkWallet
    private val logger = KotlinLogging.logger {}
    private var electrumClient: ElectrumClient? = null
    private var kyoto: Kyoto? = null
    private val transactionTimestamps: CopyOnWriteArrayList<Instant> = CopyOnWriteArrayList()
    private val db: Persister = Persister.newSqlite(dbFilePath)
    private val descriptor: Descriptor = Descriptor(descriptorString, NetworkKind.TEST)
    private val kyotoCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        wallet =
            BdkWallet.createSingle(
                descriptor = descriptor,
                network = network,
                persister = db,
                lookahead = 1000u,
            )
        val fundingAddress = wallet.peekAddress(KeychainKind.EXTERNAL, 0u)
        logger.info {
            "Funding address: ${fundingAddress.address} at index ${fundingAddress.index}"
        }

        if (syncType == SyncType.CBF) {
            val dataDir = run {
                val currentDirectory = System.getProperty("user.dir")
                "$currentDirectory/cbfdata"
            }
            logger.info { "Storing CBF data at $dataDir" }
            this.kyoto = Kyoto.create(wallet, dataDir, network)
        }
        logger.info { "Wallet initialized" }

        if (syncType == SyncType.CBF) {
            logger.info { "Starting Compact Block Filter Node" }
            kyotoStart()
        } else {
            logger.info { "Connecting to Electrum server at $electrumUrl" }
            this.electrumClient = ElectrumClient(electrumUrl)
            electrumFullScan()
        }

        logger.info { "Faucet amount: $faucetAmount sats per request" }
    }

    fun sync() {
        // Compact Block Filter client doesn't need to sync this way
        if (this.syncType == SyncType.ELECTRUM) {
            electrumSync()
        }
    }

    fun getBalance(): ULong {
        return wallet.balance().total.toSat()
    }

    fun getReport(): FaucetReport {
        val now = Instant.now()
        val snapshots = transactionTimestamps.toList()

        val last7Days = snapshots.count { it.isAfter(now.minus(7, ChronoUnit.DAYS)) }
        val last30Days = snapshots.count { it.isAfter(now.minus(30, ChronoUnit.DAYS)) }

        val today = LocalDate.now(ZoneOffset.UTC)
        val currentMonth = YearMonth.of(today.year, today.month)
        val months =
            (0L..2L).map { offset ->
                val month = currentMonth.minusMonths(offset)
                val start = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                val end = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                MonthlyStats(
                    month = month.toString(),
                    count = snapshots.count { it >= start && it < end },
                )
            }

        val report =
            FaucetReport(
                versionName = versionName,
                balanceSats = getBalance(),
                last7Days = last7Days,
                last30Days = last30Days,
                months = months,
            )
        logger.info { "Report: $report" }
        return report
    }

    fun sendTo(address: String) {
        logger.info { "Attempting to send coins to address '$address'" }

        val psbt: Psbt =
            try {
                val recipient = Address(address, network)
                val psbt: Psbt =
                    TxBuilder()
                        .addRecipient(recipient.scriptPubkey(), Amount.fromSat(faucetAmount))
                        .feeRate(FeeRate.fromSatPerVb(8uL))
                        .finish(wallet)

                wallet.sign(psbt)
                psbt
            } catch (e: Exception) {
                logger.error(e) { "Failed to build transaction for address '$address'" }
                throw e
            }

        try {
            val transaction = psbt.extractTx()
            val timestamp: ULong = Instant.now().epochSecond.toULong()
            val unconfirmedTx = UnconfirmedTx(tx = transaction, lastSeen = timestamp)

            if (syncType == SyncType.ELECTRUM) {
                electrumBroadcast(transaction)
            } else if (syncType == SyncType.CBF) {
                kyotoBroadcast(transaction, unconfirmedTx)
            }

            transactionTimestamps.add(Instant.now())
        } catch (e: Exception) {
            logger.error(e) { "Failed to broadcast transaction for address '$address'" }
            throw e
        }
    }

    private fun kyotoStart() {
        logger.info { "Starting Kyoto" }
        kyoto!!.writeLogs()
        val updatesFlow = kyoto!!.start()
        kyotoCoroutineScope.launch {
            updatesFlow.collect {
                wallet.applyUpdate(it)
                wallet.persist(db)
            }
        }
    }

    private fun electrumFullScan() {
        logger.info { "Performing Electrum initial full scan" }
        val fullScanRequest = wallet.startFullScan().build()
        val update =
            electrumClient?.fullScan(
                request = fullScanRequest,
                stopGap = 100uL,
                batchSize = 10uL,
                fetchPrevTxouts = true,
            ) ?: throw IllegalStateException()
        wallet.applyUpdate(update)
        wallet.persist(db)
    }

    private fun electrumSync() {
        val syncRequest = wallet.startSyncWithRevealedSpks().inspectSpks(SyncCallback()).build()
        val start = System.currentTimeMillis()
        val update =
            electrumClient?.sync(
                request = syncRequest,
                batchSize = 10uL,
                fetchPrevTxouts = true,
            ) ?: throw IllegalStateException()
        val elapsed = (System.currentTimeMillis() - start) / 1000.0
        wallet.applyUpdate(update)
        wallet.persist(db)
        val balance = wallet.balance().total.toSat()
        logger.info { "Sync completed in ${elapsed}s. Balance: $balance sats" }
    }

    private fun electrumBroadcast(transaction: Transaction) {
        // Clean up this exception
        electrumClient?.transactionBroadcast(transaction) ?: throw IllegalStateException()
        logger.info { "Broadcast tx ${transaction.computeTxid()} using Electrum" }
    }

    private fun kyotoBroadcast(transaction: Transaction, unconfirmedTx: UnconfirmedTx) {
        logger.info { "Broadcast tx ${transaction.computeTxid()} using Kyoto" }
        kyotoCoroutineScope.launch { kyoto!!.broadcast(transaction) }
        wallet.applyUnconfirmedTxs(listOf(unconfirmedTx))
        // Remove this log after testing
        logger.info {
            "Kyoto applied unconfirmed transaction ${transaction.computeTxid()} to wallet"
        }
    }
}

class SyncCallback : SyncScriptInspector {
    private val logger = KotlinLogging.logger {}
    var totalSynced = 0

    // On the first run of the callback, log the number of scripts that will be inspected.
    override fun inspect(script: Script, total: ULong) {
        if (totalSynced == 0) logger.info { "Syncing $total scripts in this sync" }
        totalSynced++
    }
}
