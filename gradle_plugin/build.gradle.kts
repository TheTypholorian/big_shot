plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://typho.net/maven")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")

    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
    implementation("org.ow2.asm:asm-util:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")

    implementation("org.apache.maven:maven-model:3.9.11")
    implementation("org.apache.maven.resolver:maven-resolver-api:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-util:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-impl:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-transport-file:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-supplier-mvn3:2.0.21")

    implementation("net.typho:asm_util:${rootProject.property("versions.asm_util")}")
    implementation("net.typho:data_util:${rootProject.property("versions.data_util")}")
    implementation("net.typho:big_shot.common:1.0.1")
    implementation("net.fabricmc:class-tweaker:0.3.0")
}

gradlePlugin {
    plugins {
        create("big_shot_plugin") {
            id = "net.typho.big_shot.plugin"
            implementationClass = "net.typho.big_shot.plugin.BigShotSettingsPlugin"
        }

        create("big_shot_config_plugin") {
            id = "net.typho.big_shot.plugin.config"
            implementationClass = "net.typho.big_shot.plugin.BigShotConfigPlugin"
        }
    }
}