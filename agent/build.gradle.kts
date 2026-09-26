plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.2.0"
    id("net.typho.big_shot.plugin") version "1.0.0"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.neoforged.net/releases")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://typho.net/maven")
}

dependencies {
    extraAccessWiden("net.fabricmc:fabric-loader:0.19.5") // TODO
    extraAccessWiden("net.neoforged.fancymodloader:loader:11.0.23") // TODO

    shadow(kotlin("stdlib"))

    compileOnly("org.ow2.asm:asm:9.10.1")
    compileOnly("org.ow2.asm:asm-tree:9.10.1")
    compileOnly("org.ow2.asm:asm-util:9.10.1")
    compileOnly("org.ow2.asm:asm-commons:9.10.1")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("org.spongepowered:mixin:0.8.5")
    implementation("io.github.llamalad7:mixinextras-fabric:0.6.0")
    shadow(implementation("net.fabricmc:class-tweaker:0.3.0") {
        isTransitive = false
    })
    shadow(implementation(project(":common")) {
        isTransitive = false
    })
    shadow(implementation("net.typho:asm_util:${rootProject.property("versions.asm_util")}") {
        isTransitive = false
    })
    shadow(implementation("net.typho:data_util:${rootProject.property("versions.data_util")}") {
        isTransitive = false
    })
    shadow(implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.2.0") {
        isTransitive = false
    })
    shadow(implementation(kotlin("reflect"))!!)
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(this@Project.configurations.shadow.get())

    from(project(":api").tasks.named("shadowJar")) {
        into("big_shot")
    }

    manifest {
        attributes(
            "Premain-Class" to "net.typho.big_shot.agent.BigShotAgent",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Can-Set-Native-Method-Prefix" to "true"
        )
    }
}