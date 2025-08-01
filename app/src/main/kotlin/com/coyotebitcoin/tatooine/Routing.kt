/*
 * Copyright 2020-2025 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.ShutDownUrl
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory

fun Application.configureRouting(wallet: FaucetWallet) {
    val logger = LoggerFactory.getLogger("FAUCET_LOGS")

    routing {
        authenticate {
            get("/") {
                logger.info("'/' (root) route accessed")
                call.respondText(
                    text = "Do. Or do not. There is no try.\n",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            }

            get("/getbalance") {
                logger.debug("'getbalance' route accessed")
                wallet.sync()
                val balance: String = wallet.getBalance().toString()

                call.respondText(
                    text = "Balance is $balance\n",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
            }

            post("/sendcoins") {
                logger.debug("'sendcoins' route accessed")
                val address: String = async { call.receive<String>() }.await()
                try {
                    wallet.sendTo(address)
                } catch (e: Exception) {
                    call.respondText(
                        text = "Error sending coins to $address",
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.InternalServerError
                    )
                    return@post
                }
                call.respondText(
                    text = "Sending coins to $address",
                    contentType = ContentType.Text.Plain,
                    status = HttpStatusCode.OK
                )
                wallet.sync()
            }

            val shutdown = ShutDownUrl("") { 1 }
            get("/shutdown") {
                logger.info("'shutdown' route accessed: shutting down server")
                shutdown.doShutdown(call)
            }
        }
    }
}
