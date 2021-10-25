package com.goldenraven.tatooine

import org.bitcoindevkit.bdkjni.*
import java.nio.file.Paths

object TatooineWallet {
    
    private val lib: Lib
    private lateinit var walletPtr: WalletPtr
    private const val name: String = "padawan-faucet-0"
    private const val electrumURL: String = "ssl://electrum.blockstream.info:60002"

    private const val feeRate: Float = 1F
    private const val amountToSend: String = 21000.toString()


    init {
        Lib.load()
        lib = Lib()
    }        

    fun initializeWallet(descriptor: String, change_descriptor: String) {
        walletPtr = Lib().constructor(
            WalletConstructor(
                name = name,
                network = Network.testnet,
                path = getDataDir(),
                descriptor = descriptor,
                change_descriptor = change_descriptor,
                electrum_url = electrumURL,
                electrum_proxy = null,
                electrum_retry = 10,
                electrum_timeout = null,
                electrum_stop_gap = 100,
            )
        )
    }

    fun sync(max_address: Int?=null) {
        lib.sync(walletPtr, max_address)
    }

    fun getBalance(): Long {
        return lib.get_balance(walletPtr)
    }

    fun generateNewAddress(): String {
        return lib.get_new_address(walletPtr)
    }

    private fun createTransaction(
        fee_rate: Float,
        addressees: List<Pair<String, String>>,
        send_all: Boolean? = false,
        utxos: List<String>? = null,
        unspendable: List<String>? = null,
        policy: Map<String, List<String>>? = null,
    ): CreateTxResponse {
        return lib.create_tx(walletPtr, fee_rate, addressees, send_all, utxos, unspendable, policy)
    }

    private fun sign(psbt: String, assume_height: Int? = null): SignResponse {
        return lib.sign(walletPtr, psbt, assume_height)
    }

    private fun extractPsbt(psbt: String): RawTransaction {
        return lib.extract_psbt(walletPtr, psbt)
    }

    private fun broadcast(raw_tx: String): Txid {
        return lib.broadcast(walletPtr, raw_tx)
    }

    fun sendTo(address: String): String {
        val addresseesAndAmounts: List<Pair<String, String>> = listOf(Pair(address, amountToSend))
        val transactionDetails = createTransaction(feeRate,addresseesAndAmounts, false, null, null, null)
        val signResponse: SignResponse = sign(transactionDetails.psbt)
        val rawTx: RawTransaction = extractPsbt(signResponse.psbt)
        val txid: Txid = broadcast(rawTx.transaction)
        return txid.txid
    }

    private fun getDataDir(): String {
        return Paths.get(System.getProperty("java.io.tmpdir"), "tatooine-0").toString()
    }
}
