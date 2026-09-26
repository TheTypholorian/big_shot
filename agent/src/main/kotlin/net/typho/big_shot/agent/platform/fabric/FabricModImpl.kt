package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModDependency
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.common.BigShotModData
import net.typho.data_util.DataReadException
import net.typho.data_util.impl.JsonFormat
import java.io.InputStream
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.jvm.optionals.getOrNull

data class FabricModImpl(
    @JvmField
    val fabric: ModContainer
) : PlatformMod() {
    override val id: String
        get() = fabric.metadata.id
    override val version: String
        get() = fabric.metadata.version.friendlyString
    override val bigShotData: BigShotModData? by lazy {
        findResource(BigShotModData.FILE_NAME)?.use {
            try {
                val data = JsonFormat().read(BigShotModData.CODEC, it.bufferedReader().readText())

                if (id != "big_shot" && fabric.metadata.dependencies.none { it.modId == "big_shot" && it.kind == ModDependency.Kind.DEPENDS }) {
                    LOG.warn("Mod $id has big shot metadata but doesn't declare a dependency on big shot")
                }

                data
            } catch (e: DataReadException) {
                LOG.error("Error reading big shot mod data for mod $id", e)
                return@use null
            }
        }
    }

    override fun findResource(file: String): InputStream? {
        return fabric.findPath(file).getOrNull()?.inputStream()
    }
}