package net.typho.big_shot.plugin.ext

import net.typho.big_shot.common.MinecraftVersionManifest
import net.typho.big_shot.plugin.BigShotSettingsPlugin
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

abstract class BigShotSettingsExtension @Inject constructor(
    objects: ObjectFactory,
    private val plugin: BigShotSettingsPlugin,
    private val settings: Settings,
    cacheFolder: File
) {
    @JvmField
    var versionManifest = MinecraftVersionManifest.fromCache(cacheFolder)

    fun requireVersionIsKnown(version: String) {
        if (!versionManifest.versions.any { it.id == version }) {
            versionManifest = versionManifest.redownload()

            if (!versionManifest.versions.any { it.id == version }) {
                throw IllegalStateException("Redownloaded Minecraft version manifest yet version '$version' is still missing")
            }
        }
    }

    fun includeMod(name: String, vararg targets: String) {
        println("Mod $name")
        settings.pluginManagement {
            it.includeBuild(name)
            it.plugins.id("big_shot.config.${name}").version("1.0.0")
        }
        targets.forEach { settings.include(it) }

        settings.gradle.beforeProject { project ->
            if (targets.contains(project.path)) {
                println("Applying to project ${project.path}, mod $name")
                plugin.applyTarget(project, name)
            }
        }
    }
}