plugins {
    kotlin("jvm")
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
    implementation("org.ow2.asm:asm-util:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")
    implementation("net.typho:asm_util:${rootProject.property("versions.asm_util")}")
}

kotlin {
    jvmToolchain(25)
}