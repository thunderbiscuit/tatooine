/*
 * Copyright 2020-2026 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.coyotebitcoin.tatooine.config

import kotlinx.serialization.Serializable
import org.bitcoindevkit.Network

@Serializable
data class TatooineConfig(
    val ktor: KtorConfig,
    val faucet: FaucetConfig,
    val wallet: WalletConfig,
)

@Serializable data class KtorConfig(val deployment: DeploymentConfig)

@Serializable
data class DeploymentConfig(
    val host: String,
    val port: Int,
)

@Serializable
data class FaucetConfig(
    val bearerToken: String,
    val amount: ULong,
    val versionName: String,
)

@Serializable
data class WalletConfig(
    val descriptor: String,
    val network: String,
    val electrumUrl: String,
    val syncType: String,
)

enum class SyncType {
    CBF,
    ELECTRUM,
}

fun String.toSyncType(): SyncType =
    when (this) {
        "CBF" -> SyncType.CBF
        "Electrum" -> SyncType.ELECTRUM
        else -> throw IllegalArgumentException("Unsupported sync type: $this")
    }

fun String.toNetwork(): Network =
    when (this) {
        "REGTEST" -> Network.REGTEST
        "SIGNET" -> Network.SIGNET
        "TESTNET" -> Network.TESTNET
        "TESTNET4" -> Network.TESTNET4
        else -> throw IllegalArgumentException("Unsupported network: $this")
    }
