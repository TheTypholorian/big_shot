package net.typho.big_shot.plugin

import net.typho.big_shot.common.MinecraftVersionManifest
import java.io.File
import javax.inject.Inject

abstract class BigShotBuildService @Inject constructor(
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
}