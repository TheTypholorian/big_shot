rootProject.name = "big_shot"

pluginManagement {
    includeBuild("gradle_plugin")

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
    id("net.typho.big_shot.plugin") version "1.0.0"
    id("net.typho.typho_publish") version "1.0.3" apply false
}

includeBuild("common")
include("decompiler")
include("merger")
include("test_mod")

bigShot {
    includeMod("agent_config", ":agent")
    includeMod("api_config", ":api")
    //includeMod("test_mod_config", ":test_mod")
}