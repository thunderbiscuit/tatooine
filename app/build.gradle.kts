plugins {
    id("org.gradle.application")
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
}

group = "com.goldenraven"
version = "0.7.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:2.3.9")
    implementation("io.ktor:ktor-server-auth:2.2.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.3")

    // bitcoindevkit
    // implementation("org.bitcoindevkit:bdk-jvm:0.25.0")
    implementation("org.bitcoindevkit:bdk-jvm:1.0.0-alpha.2b-SNAPSHOT")

    // tests
    // testImplementation("io.ktor:ktor-server-tests:2.4.0")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

distributions {
    main {
        distributionBaseName.set("tatooine")
    }
}
