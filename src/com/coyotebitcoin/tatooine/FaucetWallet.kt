/*
 * Copyright 2020-2026 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.serialization.Serializable
import org.bitcoindevkit.Address
import org.bitcoindevkit.Amount
import org.bitcoindevkit.Descriptor
import org.bitcoindevkit.ElectrumClient
import org.bitcoindevkit.FeeRate
import org.bitcoindevkit.Network
import org.bitcoindevkit.NetworkKind
import org.bitcoindevkit.Persister
import org.bitcoindevkit.Psbt
import org.bitcoindevkit.Script
import org.bitcoindevkit.SyncScriptInspector
import org.bitcoindevkit.TxBuilder
import org.bitcoindevkit.Wallet as BdkWallet

@Serializable data class MonthlyStats(val month: String, val count: Int)

@Serializable
data class FaucetReport(
    val balanceSats: ULong,
    val last7Days: Int,
    val last30Days: Int,
    val months: List<MonthlyStats>,
) {
    override fun toString(): String =
        "balance=$balanceSats sats, last7Days=$last7Days, last30Days=$last30Days, months=$months"
}

class FaucetWallet(
    descriptorString: String,
    private val network: Network,
    electrumUrl: String,
    private val faucetAmount: ULong,
    dbFilePath: String,
) {
    private val wallet: BdkWallet
    private val logger = KotlinLogging.logger {}
    private val electrumClient: ElectrumClient = ElectrumClient(electrumUrl)
    private val transactionTimestamps: CopyOnWriteArrayList<Instant> = CopyOnWriteArrayList()
    private val db: Persister = Persister.newSqlite(dbFilePath)
    private val descriptor: Descriptor = Descriptor(descriptorString, NetworkKind.TEST)

    init {
        wallet =
            BdkWallet.createSingle(
                descriptor = descriptor,
                network = network,
                persister = db,
            )
        logger.info { "Wallet initialized" }
        logger.info { "Connecting to Electrum server at $electrumUrl" }
        logger.info { "Faucet amount: $faucetAmount sats per request" }
        fullScan()
    }

    private fun fullScan() {
        logger.info { "First full scan of wallet" }
        val fullScanRequest = wallet.startFullScan().build()
        val update =
            electrumClient.fullScan(
                request = fullScanRequest,
                stopGap = 100uL,
                batchSize = 10uL,
                fetchPrevTxouts = true,
            )
        wallet.applyUpdate(update)
        wallet.persist(db)
    }

    fun sync() {
        logger.info { "Syncing wallet" }
        val syncRequest = wallet.startSyncWithRevealedSpks().inspectSpks(SyncCallback()).build()
        val start = System.currentTimeMillis()
        val update =
            electrumClient.sync(
                request = syncRequest,
                batchSize = 10uL,
                fetchPrevTxouts = true,
            )
        val elapsed = (System.currentTimeMillis() - start) / 1000.0
        wallet.applyUpdate(update)
        wallet.persist(db)
        val balance = wallet.balance().total.toSat()
        logger.info { "Sync completed in ${elapsed}s. Balance: $balance sats" }
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
            electrumClient.transactionBroadcast(psbt.extractTx())
            transactionTimestamps.add(Instant.now())
            logger.info { "Faucet sent coins to address '$address'" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to broadcast transaction for address '$address'" }
            throw e
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

fun String.toNetwork(): Network =
    when (this) {
        "REGTEST" -> Network.REGTEST
        "SIGNET" -> Network.SIGNET
        "TESTNET" -> Network.TESTNET
        "TESTNET4" -> Network.TESTNET4
        else -> throw IllegalArgumentException("Unsupported network: $this")
    }
