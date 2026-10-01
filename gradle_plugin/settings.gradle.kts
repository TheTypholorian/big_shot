rootProject.name = "gradle_plugin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://maven.fabricmc.net")
        maven("https://typho.net/maven")
    }
}

plugins {
    kotlin("jvm") version "2.4.0" apply false
    id("net.typho.typho_publish") version "1.0.4" apply false
}

includeFlat("common")