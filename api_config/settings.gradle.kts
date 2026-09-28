pluginManagement {
    includeBuild("../gradle_plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://maven.fabricmc.net")
        maven("https://typho.net/maven")
    }
}