rootProject.name = "big_shot"

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
}

include("agent")
include("api")
include("common")
include("decompiler")
includeBuild("gradle_plugin")
include("merger")
include("test_mod")