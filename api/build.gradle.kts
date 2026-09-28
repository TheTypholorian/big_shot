plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.2.0"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.fabricmc.net")
    maven("https://typho.net/maven")
}

dependencies {
    extraAccessWiden("net.fabricmc:fabric-loader:0.19.5") // TODO
    compileOnly("org.spongepowered:mixin:0.8.5")

    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
    implementation("org.ow2.asm:asm-util:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("org.apache.commons:commons-lang3:3.20.0")
    implementation("io.github.llamalad7:mixinextras-fabric:0.6.0")
    implementation("net.typho:asm_util:${rootProject.property("versions.asm_util")}") {
        isTransitive = false
    }
    implementation("net.typho:data_util:${rootProject.property("versions.data_util")}") {
        isTransitive = false
    }
    implementation("net.typho:big_shot.common:1.0.1") {
        isTransitive = false
    }
    implementation("net.fabricmc:class-tweaker:0.3.0") {
        isTransitive = false
    }
    implementation(kotlin("reflect"))
    compileOnly(project(":agent")) {
        isTransitive = false
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveVersion.set("")
    archiveClassifier.set("")
    configurations = listOf(this@Project.configurations.shadow.get())

    from(this@Project.configurations.compileClasspath) {
        include("mixinextras-fabric-*.jar") // TODO
        into("big_shot")
    }
}