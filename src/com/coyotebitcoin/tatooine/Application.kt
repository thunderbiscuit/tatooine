/*
 * Copyright 2020-2026 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine

import com.coyotebitcoin.tatooine.config.TatooineConfig
import com.coyotebitcoin.tatooine.config.toNetwork
import com.coyotebitcoin.tatooine.config.toSyncType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.config.getAs
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config = environment.config.getAs<TatooineConfig>()
    val faucetConfig = config.faucet
    val walletConfig = config.wallet
    logger.info {
        "Server starting on ${config.ktor.deployment.host}:${config.ktor.deployment.port}"
    }
    logger.info { "Faucet version: ${faucetConfig.versionName}" }
    logger.info { "Faucet wallet syncs with ${walletConfig.syncType} client" }

    // All persistent state lives in one place regardless of where the jar is launched from.
    // Keep in sync with the log file location in logback.xml.
    val dataDir: Path = Path.of(System.getProperty("user.home"), ".tatooine")
    Files.createDirectories(dataDir)
    logger.info { "Data directory: $dataDir" }

    val faucetWallet =
        FaucetWallet(
            descriptorString = walletConfig.descriptor,
            network = walletConfig.network.toNetwork(),
            electrumUrl = walletConfig.electrumUrl,
            syncType = walletConfig.syncType.toSyncType(),
            faucetAmount = faucetConfig.amount,
            versionName = faucetConfig.versionName,
            dataDir = dataDir,
        )

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        bearer {
            realm = "Protected"
            authenticate { tokenCredential ->
                if (tokenCredential.token == faucetConfig.bearerToken) {
                    UserIdPrincipal("User")
                } else {
                    logger.warn {
                        "bad authenticated request made with token '${tokenCredential.token}'"
                    }
                    null
                }
            }
        }
    }

    configureRouting(wallet = faucetWallet)
}
