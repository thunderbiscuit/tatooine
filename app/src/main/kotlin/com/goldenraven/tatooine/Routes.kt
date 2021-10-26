package com.goldenraven.tatooine

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*

fun Route.root() {
    get("/") {
        call.application.environment.log.info("root route accessed")
        call.respondText("Do. Or do not. There is no try.\n", ContentType.Text.Plain)
    }
}

fun Route.newAddress(wallet: TatooineWallet) {
    get("/newaddress") {
        val newAddress = wallet.generateNewAddress()
        call.application.environment.log.info("newaddress route accessed with address: $newAddress")
        call.respondText("Load wallet by sending testnet coins to $newAddress\n", ContentType.Text.Plain)
        wallet.sync()
    }
}

fun Route.getBalance(wallet: TatooineWallet) {
    get("/getbalance") {
        wallet.sync()
        val balance: String = wallet.getBalance().toString()
        call.application.environment.log.info("getbalance route accessed with balance: $balance")
        call.respondText("Balance is $balance\n", ContentType.Text.Plain)
    }
}

fun Route.sendCoins(wallet: TatooineWallet) {
    post("/sendcoins") {
        val address: String = call.receiveText()
        val txid = wallet.sendTo(address)
        call.application.environment.log.info("sendcoins route accessed with txid $txid")
        call.respondText("Sent coins to $address\ntxid: $txid\n", ContentType.Text.Plain)
        wallet.sync()
    }
}

val shutdown = ShutDownUrl("") { 1 }

fun Route.shutdown() {
    get("/shutdown") {
        shutdown.doShutdown(call)
    }
}
