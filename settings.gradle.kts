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

include("agent")
include("data")
include("decompiler")
includeBuild("gradle_plugin")
include("api")
include("merger")
//include("test_mod")