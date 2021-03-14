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

    val tatooineWallet: TatooineWallet = TatooineWallet()

    routing {
        get("/") {
            tatooineWallet.helloWallet()
            call.respondText("Do. Or do not. There is no try.", ContentType.Text.Plain)
        }
    }
}
