package com.goldenraven.tatooine

import org.bitcoindevkit.*
import org.bitcoindevkit.Wallet as BdkWallet

object FaucetWallet {
    private lateinit var wallet: BdkWallet
    private const val electrumURL: String = "ssl://electrum.blockstream.info:60002"
    private const val faucetAmount: ULong = 75000uL

    private val blockchainConfig = BlockchainConfig.Electrum(
        ElectrumConfig(
            url = electrumURL,
            socks5 = null,
            retry = 10u,
            timeout = null,
            stopGap = 10u
        )
    )
    private val blockchain: Blockchain = Blockchain(blockchainConfig)

    fun initializeWallet(descriptor: String, changeDescriptor: String) {
        val database = DatabaseConfig.Sqlite(SqliteDbConfiguration("./bdk-sqlite"))

        this.wallet = BdkWallet(
            descriptor = descriptor,
            changeDescriptor = changeDescriptor,
            network = Network.TESTNET,
            databaseConfig = database,
        )
    }

    fun sync() {
        wallet.sync(blockchain = blockchain, progress = NullProgress)
    }

    fun getBalance(): ULong {
        return wallet.getBalance().total
    }

    fun sendTo(address: String): String {
        val (psbt, txDetails) = TxBuilder()
            .addRecipient(Address(address).scriptPubkey(), faucetAmount)
            .feeRate(2.0f)
            .finish(wallet)
        wallet.sign(psbt)
        blockchain.broadcast(psbt)
        return txDetails.txid
    }
}

object NullProgress : Progress {
    override fun update(progress: Float, message: String?) {}
}
