import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.goldenraven"
version = "0.2.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    // implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // bitcoindevkit
    implementation("org.bitcoindevkit.bdkjni:bdk-jvm:0.2.1-dev")

    // tests
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.getMainClass()
            )
        )
    }
}
