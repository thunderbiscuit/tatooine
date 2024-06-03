plugins {
    id("org.gradle.application")
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
}

group = "com.goldenraven"
version = "0.10.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:2.3.9")
    implementation("io.ktor:ktor-server-auth:2.2.1")

    // logging
    implementation("ch.qos.logback:logback-classic:1.5.3")

    // bitcoindevkit
    implementation("org.bitcoindevkit:bdk-jvm:1.0.0-alpha.9")

    // tests
    // testImplementation("io.ktor:ktor-server-tests:2.4.0")
}

distributions {
    main {
        distributionBaseName.set("tatooine")
    }
}
