plugins {
    kotlin("jvm")
    id("net.typho.big_shot.plugin") version "1.0.0"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://typho.net/maven")
}

dependencies {
    implementation(project(":api")) // TODO
}

kotlin {
    jvmToolchain(25)
}