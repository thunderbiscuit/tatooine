/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */
 
package com.goldenraven.tatooine

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val apiPassword: String = environment.config.property("wallet.apiPassword").getString()
    val descriptor = environment.config.property("wallet.descriptor").getString()
    val changeDescriptor = environment.config.property("wallet.changeDescriptor").getString()
    
    // Initialize wallet
    val tatooineWallet: TatooineWallet = TatooineWallet
    tatooineWallet.initializeWallet(descriptor, changeDescriptor)
    tatooineWallet.sync()

    install(Authentication) {
        basic(name = "padawan-authenticated") {
            realm = "Ktor Server"
            validate { credentials ->
                println("Authenticated request made with credentials $credentials")
                if (credentials.password == apiPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    routing {
        // non-authenticated routes
        root()

        // authenticated routes
        authenticate("padawan-authenticated") {
            newAddress(tatooineWallet)
            getBalance(tatooineWallet)
            sendCoins(tatooineWallet)
            shutdown()
        }
    }
}
