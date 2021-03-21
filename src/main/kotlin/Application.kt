/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */
 
package com.goldenraven

import TatooineWallet
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val needsFullSync: Boolean = environment.config.propertyOrNull("WALLET_ALREADY_SYNCED")?.getString().toBoolean().not()
    println("Wallet requires full sync: $needsFullSync")

    val tatooineWallet: TatooineWallet = TatooineWallet()
    tatooineWallet.initializeWallet(needsFullSync)

    routing {
        get("/") {
            tatooineWallet.helloWallet(needsFullSync)
            call.respondText("Do. Or do not. There is no try.", ContentType.Text.Plain)
        }

        // for testing purposes only, will remove
        get("/newaddress") {
           val newAddress = tatooineWallet.generateNewAddress()
           call.respondText("Load wallet by sending testnet coins to $newAddress", ContentType.Text.Plain)
        }

        // for testing purposes only, will remove
        get("/getbalance") {
           val balance: String = tatooineWallet.getBalance().toString()
           call.respondText("Balance is $balance", ContentType.Text.Plain)
       }

        post("/sendcoins") {
            val address: String = call.receive<String>()
            val txid = tatooineWallet.sendTo(address)
            call.respondText("Sent coins to $address\ntxid: $txid", ContentType.Text.Plain)
        }
    }
}
