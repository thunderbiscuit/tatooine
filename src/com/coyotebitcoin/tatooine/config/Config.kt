/*
 * Copyright 2020-2026 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine.config

import kotlinx.serialization.Serializable

@Serializable
data class TatooineConfig(
    val ktor: KtorConfig,
    val faucet: FaucetConfig,
    val wallet: WalletConfig,
)

@Serializable
data class KtorConfig(
    val deployment: DeploymentConfig,
)

@Serializable
data class DeploymentConfig(
    val host: String,
    val port: Int,
)

@Serializable
data class FaucetConfig(
    val bearerToken: String,
    val amount: ULong,
)

@Serializable
data class WalletConfig(
    val descriptor: String,
    val network: String,
    val electrumUrl: String,
)
