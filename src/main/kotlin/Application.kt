package com.goldenraven

import Wallet
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    routing {
        get("/") {
            val myWallet: Wallet = Wallet()
            myWallet.helloWallet()

            call.respondText("Do. Or do not. There is no try.", ContentType.Text.Plain)
        }
    }
}

