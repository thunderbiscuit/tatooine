/*
 * Copyright 2020-2023 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.tatooine

import org.bitcoindevkit.Address
import org.bitcoindevkit.Descriptor
import org.bitcoindevkit.EsploraClient
import org.bitcoindevkit.FeeRate
import org.bitcoindevkit.Network
import org.bitcoindevkit.Psbt
import org.bitcoindevkit.TxBuilder
import org.slf4j.LoggerFactory
import org.bitcoindevkit.Wallet as BdkWallet

class FaucetWallet(
    descriptorString: String,
    esploraUrl: String,
) {
    private val wallet: BdkWallet
    private val logger = LoggerFactory.getLogger("FAUCET_LOGS")
    private val faucetAmount: ULong = 75000uL
    private val esploraClient: EsploraClient = EsploraClient(esploraUrl)

    init {
        val dbFilePath = run {
            val currentDirectory = System.getProperty("user.dir")
            "$currentDirectory/bdk_persistence.db"
        }
        val descriptor: Descriptor = Descriptor(descriptorString, Network.TESTNET)

        wallet = BdkWallet(
            descriptor = descriptor,
            changeDescriptor = null,
            persistenceBackendPath = dbFilePath,
            network = Network.TESTNET
        )
        logger.info("Wallet initialized")
    }

    fun sync() {
        logger.info("Syncing wallet")
        val update = esploraClient.fullScan(wallet, 10uL, 1uL)
        wallet.applyUpdate(update)
    }

    fun getBalance(): ULong {
        logger.info("Getting wallet balance")
        return wallet.getBalance().total
    }

    fun sendTo(address: String) {
        logger.info("Attempting to send coins to `$address`")

        val psbt: Psbt = try {
            val recipient = Address(address, Network.TESTNET)
            val psbt: Psbt = TxBuilder()
                .addRecipient(recipient.scriptPubkey(), faucetAmount)
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
            esploraClient.broadcast(psbt.extractTx())
        } catch (e: Exception) {
            // Log at ERROR level for simple logs
            logger.error("Failed to broadcast transaction for `$address`: ${e.javaClass}: ${e.message}")
            // Log with stack trace at DEBUG level for detailed debugging log file
            logger.debug("Failed to broadcast transaction for $address", e)
            throw e
        }
    }
}
