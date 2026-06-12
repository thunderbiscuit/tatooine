/*
 * Copyright 2020-2026 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */
 
package com.coyotebitcoin.tatooine

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.netty.EngineMain
import org.bitcoindevkit.Network
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val bearerToken: String = environment.config.property("wallet.bearerToken").getString()
    val descriptor = environment.config.property("wallet.descriptor").getString()
    val network: Network = environment.config.property("wallet.network").getString().toNetwork()
    val electrumUrl = environment.config.property("wallet.electrumUrl").getString()
    val amount = environment.config.property("wallet.amount").getString().toULong()
    val logger = LoggerFactory.getLogger("FAUCET_LOGS")

    // Initialize wallet
    val faucetWallet = FaucetWallet(descriptor, network, electrumUrl, amount)
    faucetWallet.sync()

    install(Authentication) {
        bearer {
            realm = "Protected"
            authenticate { tokenCredential ->
                if (tokenCredential.token == bearerToken) {
                    UserIdPrincipal("User")
                } else {
                    logger.warn("bad authenticated request made with token '${tokenCredential.token}'")
                    null
                }
            }
        }
    }

    configureRouting(wallet = faucetWallet)
}
