package com.goldenraven.tatooine

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.root() {
    get("/") {
        call.respondText("Do. Or do not. There is no try.", ContentType.Text.Plain)
    }
}

fun Route.newAddress(wallet: TatooineWallet) {
    get("/newaddress") {
        val newAddress = wallet.generateNewAddress()
        call.respondText("Load wallet by sending testnet coins to $newAddress", ContentType.Text.Plain)
        wallet.sync()
    }
}

fun Route.getBalance(wallet: TatooineWallet) {
    get("/getbalance") {
        wallet.sync()
        val balance: String = wallet.getBalance().toString()
        call.respondText("Balance is $balance", ContentType.Text.Plain)
    }
}

fun Route.sendCoins(wallet: TatooineWallet) {
    post("/sendcoins") {
        val address: String = call.receiveText()
        println("Coins being sent to address $address")
        val txid = wallet.sendTo(address)
        call.respondText("Sent coins to $address\ntxid: $txid", ContentType.Text.Plain)
        wallet.sync()
    }
}