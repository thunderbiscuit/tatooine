package com.goldenraven.tatooine

import org.bitcoindevkit.*
import org.bitcoindevkit.Wallet as BdkWallet
import java.nio.file.Paths

object FaucetWallet {

    object NullProgress : BdkProgress {
        override fun update(progress: Float, message: String?) {}
    }
    
    private lateinit var wallet: BdkWallet
    private const val name: String = "padawan-faucet-0"
    private const val electrumURL: String = "ssl://electrum.blockstream.info:60002"

    private const val feeRate: Float = 2F
    private const val amountToSend: String = 75000.toString()

    fun initializeWallet(descriptor: String, changeDescriptor: String) {
        val databaseConfig = DatabaseConfig.Sled(
            SledDbConfiguration(
                path = getDataDir(),
                treeName = name
            )
        )

        val blockchainConfig = BlockchainConfig.Electrum(
            ElectrumConfig(
                url = electrumURL,
                socks5 = null,
                retry = 10u,
                timeout = null,
                stopGap = 10u
            )
        )

        this.wallet = BdkWallet(
            descriptor = descriptor,
            changeDescriptor = changeDescriptor,
            network = Network.TESTNET,
            databaseConfig = databaseConfig,
            blockchainConfig = blockchainConfig,
        )
    }

    fun sync(max_address: Int?=null) {
        wallet.sync(NullProgress, max_address?.toUInt())
    }

    fun getBalance(): Long {
        return wallet.getBalance().toLong()
    }

    fun generateNewAddress(): String {
        return wallet.getNewAddress()
    }

    fun sendTo(address: String): String {
        val psbt = PartiallySignedBitcoinTransaction(wallet, address, amountToSend.toULong(), feeRate)
        wallet.sign(psbt)

        // return txid
        return wallet.broadcast(psbt)
    }

    private fun getDataDir(): String {
        return Paths.get(System.getProperty("java.io.tmpdir"), "tatooine-0").toString()
    }
}
