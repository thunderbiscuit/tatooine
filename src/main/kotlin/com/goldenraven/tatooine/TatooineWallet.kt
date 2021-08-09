package com.goldenraven.tatooine

import org.bitcoindevkit.bdkjni.Lib
import org.bitcoindevkit.bdkjni.Network
import org.bitcoindevkit.bdkjni.WalletConstructor
import org.bitcoindevkit.bdkjni.WalletPtr
import org.bitcoindevkit.bdkjni.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths

object TatooineWallet {
    
    private val lib: Lib
    private lateinit var walletPtr: WalletPtr
    private val name: String = "padawan-faucet"
    private val electrumURL: String = "ssl://electrum.blockstream.info:60002"
    val log: Logger = LoggerFactory.getLogger(TatooineWallet::class.java)

    init {
        Lib.load()
        lib = Lib()
    }

    val descriptor =
        "wpkh([c258d2e4/84h/1h/0h]tpubDDYkZojQFQjht8Tm4jsS3iuEmKjTiEGjG6KnuFNKKJb5A6ZUCUZKdvLdSDWofKi4ToRCwb9poe1XdqfUnP4jaJjCB2Zwv11ZLgSbnZSNecE/0/*)"
    val change_descriptor =
        "wpkh([c258d2e4/84h/1h/0h]tpubDDYkZojQFQjht8Tm4jsS3iuEmKjTiEGjG6KnuFNKKJb5A6ZUCUZKdvLdSDWofKi4ToRCwb9poe1XdqfUnP4jaJjCB2Zwv11ZLgSbnZSNecE/1/*)"

    fun initializeWallet(): Unit  {
        walletPtr = Lib().constructor(
            WalletConstructor(
                name = name,
                network = Network.testnet,
                path = getDataDir(),
                descriptor = descriptor,
                change_descriptor = change_descriptor,
                electrum_url = electrumURL,
                electrum_proxy = null,
            )
        )
    }


    public fun sync(max_address: Int?=null) {
        lib.sync(walletPtr, max_address)
    }

    public fun getBalance(): Long {
        return lib.get_balance(walletPtr)
    }

    public fun generateNewAddress(): String {
        return lib.get_new_address(walletPtr)
    }

    public fun createTransaction(
        fee_rate: Float,
        addressees: List<Pair<String, String>>,
        send_all: Boolean? = false,
        utxos: List<String>? = null,
        unspendable: List<String>? = null,
        policy: Map<String, List<String>>? = null,
    ): CreateTxResponse {
        return lib.create_tx(walletPtr, fee_rate, addressees, send_all, utxos, unspendable, policy)
    }

    public fun sign(psbt: String, assume_height: Int? = null): SignResponse {
        return lib.sign(walletPtr, psbt, assume_height)
    }

    public fun extractPsbt(psbt: String): RawTransaction {
        return lib.extract_psbt(walletPtr, psbt)
    }

    public fun broadcast(raw_tx: String): Txid {
        return lib.broadcast(walletPtr, raw_tx)
    }

    public fun sendTo(address: String): String {
        val addresseesAndAmounts: List<Pair<String, String>> = listOf(Pair(address, 21000.toString()))
        var transactionDetails=createTransaction(1F,addresseesAndAmounts, false, null, null, null)
        val signResponse: SignResponse = sign(transactionDetails.psbt)
        val rawTx: RawTransaction = extractPsbt(signResponse.psbt)
        val txid: Txid = broadcast(rawTx.transaction)
        return txid.toString()
    }

    fun getDataDir(): String {
        // return Files.createTempDirectory("bdk-test").toString()
        // return Paths.get(System.getProperty("java.io.tmpdir"), "bdk-test").toString()
        val path = Paths.get(System.getProperty("java.io.tmpdir"), "bdk-test").toString()
        return path
    }
}
