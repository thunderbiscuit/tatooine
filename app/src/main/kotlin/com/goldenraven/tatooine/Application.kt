/*
 * Copyright 2020-2023 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */
 
package com.goldenraven.tatooine

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

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

    // Initialize wallet
    val faucetWallet = FaucetWallet(descriptor)
    faucetWallet.sync()

    install(Authentication) {
        basic(name = "padawan-authenticated") {
            realm = "Access to the faucet"
            validate { credentials ->
                if (credentials.name == "padawan" && credentials.password == apiPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    application.environment.log.warn("bad authenticated request made with credentials $credentials")
                    null
                }
            }
        }
    }

    configureRouting(wallet = faucetWallet)
}
