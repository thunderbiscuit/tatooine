/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */
 
package com.goldenraven.tatooine

import com.goldenraven.tatooine.TatooineWallet
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val apiPassword: String = environment.config.property("wallet.apiPassword").getString()
    val descriptor = environment.config.property("wallet.descriptor").getString()
    val changeDescriptor = environment.config.property("wallet.changeDescriptor").getString()
    
    val tatooineWallet: TatooineWallet = TatooineWallet
    tatooineWallet.initializeWallet(descriptor,changeDescriptor)
    tatooineWallet.sync()

    install(Authentication) {
        basic(name = "padawan-authenticated") {
            realm = "Ktor Server"
            validate { credentials ->
                println(credentials)
                if (credentials.password == apiPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    routing {

        get("/") {
            tatooineWallet.sync()
            call.respondText("Do. Or do not. There is no try.", ContentType.Text.Plain)
        }

        // for testing purposes only, will remove
        get("/newaddress") {
           tatooineWallet.sync() 
           val newAddress = tatooineWallet.generateNewAddress()
           call.respondText("Load wallet by sending testnet coins to $newAddress", ContentType.Text.Plain)
        }

        // for testing purposes only, will remove
        get("/getbalance") {
           tatooineWallet.sync() 
           val balance: String = tatooineWallet.getBalance().toString()
           call.respondText("Balance is $balance", ContentType.Text.Plain)
        }

        authenticate("padawan-authenticated") {
            post("/sendcoins") {
                tatooineWallet.sync()
                val address: String = call.receiveText()
                println("Coins being sent to address $address")
                val txid = tatooineWallet.sendTo(address)
                call.respondText("Sent coins to $address\ntxid: $txid", ContentType.Text.Plain)
            }
        }
    }
}
