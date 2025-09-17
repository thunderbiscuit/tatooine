plugins {
    id("org.gradle.application")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

group = "com.coyotebitcoin"
version = "0.14.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:3.2.2")
    implementation("io.ktor:ktor-server-auth:3.2.2")

    // logging
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // bitcoindevkit
    implementation("org.bitcoindevkit:bdk-jvm:2.0.0")

    // tests
    // testImplementation("io.ktor:ktor-server-tests:2.4.0")
}

distributions {
    main {
        distributionBaseName.set("tatooine")
    }
}
