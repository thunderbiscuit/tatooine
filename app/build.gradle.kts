plugins {
    id("org.gradle.application")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
}

group = "com.coyotebitcoin"
version = "0.13.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.ktor:ktor-server-auth:3.0.3")

    // logging
    implementation("ch.qos.logback:logback-classic:1.5.3")

    // bitcoindevkit
    implementation("org.bitcoindevkit:bdk-jvm:1.0.0-beta.6")

    // tests
    // testImplementation("io.ktor:ktor-server-tests:2.4.0")
}

distributions {
    main {
        distributionBaseName.set("tatooine")
    }
}
