/*
 * Copyright 2020-2025 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine

import org.bitcoindevkit.Address
import org.bitcoindevkit.Descriptor
import org.bitcoindevkit.ElectrumClient
import org.bitcoindevkit.Psbt
import org.bitcoindevkit.TxBuilder
import org.bitcoindevkit.Connection
import org.bitcoindevkit.Wallet as BdkWallet
import org.bitcoindevkit.Amount
import org.bitcoindevkit.Network
import org.bitcoindevkit.FeeRate
import org.slf4j.LoggerFactory

class FaucetWallet(
    descriptorString: String,
    changeDescriptorString: String,
    electrumUrl: String,
    private val faucetAmount: ULong
) {
    private val wallet: BdkWallet
    private val logger = LoggerFactory.getLogger("FAUCET_LOGS")
    // private val esploraClient: EsploraClient = EsploraClient(esploraUrl)
    private val electrumClient: ElectrumClient = ElectrumClient(electrumUrl)

    init {
        val dbFilePath = run {
            val currentDirectory = System.getProperty("user.dir")
            "$currentDirectory/bdk_persistence.db"
        }
        val descriptor: Descriptor = Descriptor(descriptorString, Network.SIGNET)
        val changeDescriptor: Descriptor = Descriptor(changeDescriptorString, Network.SIGNET)
        val connection: Connection = Connection(dbFilePath)

        wallet = BdkWallet(
            descriptor = descriptor,
            changeDescriptor = changeDescriptor,
            network = Network.SIGNET,
            connection = connection,
        )
        logger.info("Wallet initialized")
        fullScan()
    }

    private fun fullScan() {
        logger.info("First full scan of wallet")
        val fullScanRequest = wallet.startFullScan().build()
        val update = electrumClient.fullScan(
            request = fullScanRequest,
            stopGap = 100uL,
            batchSize = 10uL,
            fetchPrevTxouts = true
        )
        wallet.applyUpdate(update)
    }

    fun sync() {
        logger.info("Syncing wallet")
        val syncRequest = wallet.startSyncWithRevealedSpks().build()
        val update = electrumClient.sync(
            request = syncRequest,
            batchSize = 10uL,
            fetchPrevTxouts = true
        )
        wallet.applyUpdate(update)
        val balance = wallet.balance().total.toSat()
        logger.info("Wallet synced, balance: $balance")
    }

    fun getBalance(): ULong {
        logger.info("Getting wallet balance")
        return wallet.balance().total.toSat()
    }

    fun sendTo(address: String) {
        logger.info("Attempting to send coins to `$address`")

        val psbt: Psbt = try {
            val recipient = Address(address, Network.SIGNET)
            val psbt: Psbt = TxBuilder()
                .addRecipient(recipient.scriptPubkey(), Amount.fromSat(faucetAmount))
                .feeRate(FeeRate.fromSatPerVb(8uL))
                .finish(wallet)

            wallet.sign(psbt)
            psbt
        } catch (e: Exception) {
            // Log at ERROR level for simple logs
            logger.error("Failed to build transaction for `$address`: ${e.javaClass}: ${e.message}")
            // Log with stack trace at DEBUG level for detailed debugging log file
            logger.debug("Failed to build transaction for $address", e)
            throw e
        }

        try {
            electrumClient.transactionBroadcast(psbt.extractTx())
        } catch (e: Exception) {
            // Log at ERROR level for simple logs
            logger.error("Failed to broadcast transaction for `$address`: ${e.javaClass}: ${e.message}")
            // Log with stack trace at DEBUG level for detailed debugging log file
            logger.debug("Failed to broadcast transaction for $address", e)
            throw e
        }
    }
}
