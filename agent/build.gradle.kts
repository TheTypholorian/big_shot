plugins {
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.2.0"
    id("net.typho.big_shot.plugin") version "1.0.0"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://typho.net/maven")
}

val jij = configurations.create("jij")

dependencies {
    extraAccessWiden("net.fabricmc:fabric-loader:0.19.3") {
        isTransitive = false
    } // TODO

    jij(kotlin("stdlib"))

    compileOnly("org.ow2.asm:asm:9.10.1")
    compileOnly("org.ow2.asm:asm-tree:9.10.1")
    compileOnly("org.ow2.asm:asm-util:9.10.1")
    compileOnly("org.ow2.asm:asm-commons:9.10.1")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("org.spongepowered:mixin:0.8.5")
    jij(implementation("net.typho:asm_util:${rootProject.property("versions.asm_util")}") {
        isTransitive = false
    })
}

kotlin {
    jvmToolchain(8)
}

tasks.processResources {
    dependsOn(project(":loader").tasks.named("shadowJar"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(jij)

    manifest {
        attributes(
            "Premain-Class" to "net.typho.big_shot.agent.BigShotAgent",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Can-Set-Native-Method-Prefix" to "true"
        )
    }
}