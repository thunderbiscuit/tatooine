package com.goldenraven.tatooine

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

    private val feeRate: Float = 1F
    private val amountToSend: String = 21000.toString();


    init {
        Lib.load()
        lib = Lib()
    }        

    fun initializeWallet(descriptor: String, change_descriptor: String): Unit  {
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
        val addresseesAndAmounts: List<Pair<String, String>> = listOf(Pair(address, amountToSend))
        var transactionDetails = createTransaction(feeRate,addresseesAndAmounts, false, null, null, null)
        val signResponse: SignResponse = sign(transactionDetails.psbt)
        val rawTx: RawTransaction = extractPsbt(signResponse.psbt)
        val txid: Txid = broadcast(rawTx.transaction)
        return txid.toString()
    }

    fun getDataDir(): String {
        val path = Paths.get(System.getProperty("java.io.tmpdir"), "bdk-test5").toString()
        return path
    }
}
