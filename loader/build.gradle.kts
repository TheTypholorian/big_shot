plugins {
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.2.0"
    id("net.typho.big_shot.plugin") version "1.0.0"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://typho.net/maven")
}

val jij = configurations.create("jij")

dependencies {
    compileOnly("net.fabricmc:fabric-loader:0.19.3")

    jij(kotlin("stdlib"))
    jij(implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.2.0")!!)

    compileOnly("org.ow2.asm:asm:9.10.1")
    compileOnly("org.ow2.asm:asm-tree:9.10.1")
    compileOnly("org.ow2.asm:asm-util:9.10.1")
    compileOnly("org.ow2.asm:asm-commons:9.10.1")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("org.apache.commons:commons-lang3:3.20.0")
    extraAccessWiden("io.github.llamalad7:mixinextras-fabric:0.5.5")
    jij(implementation("net.typho:asm_util:${rootProject.property("versions.asm_util")}") {
        isTransitive = false
    })
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveVersion.set("")
    archiveClassifier.set("")
    configurations = listOf(jij)
    destinationDirectory.set(project(":agent").file("src/main/resources"))
    //dependsOn(project(":test_mod").tasks.jar)
}