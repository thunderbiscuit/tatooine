/*
 * Copyright 2020-2023 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.tatooine

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting(wallet: FaucetWallet) {
    routing {
        get("/") {
            call.application.environment.log.info("root route accessed")
            call.respondText("Do. Or do not. There is no try.\n")
        }

        authenticate("padawan-authenticated") {
            get("/getbalance") {
                wallet.sync()
                val balance: String = wallet.getBalance().toString()
                call.application.environment.log.info("getbalance route accessed with balance: $balance")
                call.respondText(
                    "Balance is $balance\n",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            }

            post("/sendcoins") {
                val address: String = call.receiveText()
                val txid = wallet.sendTo(address)
                call.application.environment.log.info("sendcoins route accessed with txid $txid")
                call.respondText("Sending coins to $address txid: $txid", ContentType.Text.Plain)
                wallet.sync()
            }

            val shutdown = ShutDownUrl("") { 1 }
            get("/shutdown") {
                shutdown.doShutdown(call)
            }
        }
    }
}
