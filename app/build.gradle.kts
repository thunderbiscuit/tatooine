plugins {
    application
    kotlin("jvm") version "1.8.0"
}

group = "com.goldenraven"
version = "0.6.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:2.2.1")
    implementation("io.ktor:ktor-server-auth:2.2.1")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // bitcoindevkit
    implementation("org.bitcoindevkit:bdk-jvm:0.25.0")

    // tests
    testImplementation("io.ktor:ktor-server-tests:2.2.1")
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
