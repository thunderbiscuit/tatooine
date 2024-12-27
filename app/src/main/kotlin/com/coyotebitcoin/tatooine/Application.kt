/*
 * Copyright 2020-2024 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */
 
package com.coyotebitcoin.tatooine

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(
        Netty, port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val apiPassword: String = environment.config.property("wallet.apiPassword").getString()
    val descriptor = environment.config.property("wallet.descriptor").getString()
    val changeDescriptor = environment.config.property("wallet.changeDescriptor").getString()
    val esploraUrl = environment.config.property("wallet.esploraUrl").getString()
    val electrumUrl = environment.config.property("wallet.electrumUrl").getString()
    val amount = environment.config.property("wallet.amount").getString().toULong()
    val logger = LoggerFactory.getLogger("FAUCET_LOGS")

    // Initialize wallet
    val faucetWallet = FaucetWallet(descriptor, changeDescriptor, electrumUrl, amount)
    faucetWallet.sync()

    install(Authentication) {
        basic(name = "padawan-authenticated") {
            realm = "Access to the faucet"
            validate { credentials ->
                if (credentials.name == "padawan" && credentials.password == apiPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    logger.info("bad authenticated request made with credentials $credentials")
                    null
                }
            }
        }
    }

    configureRouting(wallet = faucetWallet)
}
