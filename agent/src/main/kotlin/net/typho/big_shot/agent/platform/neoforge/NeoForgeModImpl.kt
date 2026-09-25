package net.typho.big_shot.agent.platform.neoforge

import net.neoforged.fml.ModContainer
import net.neoforged.neoforgespi.language.IModInfo
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.common.BigShotModData
import net.typho.data_util.DataReadException
import net.typho.data_util.impl.JsonFormat
import java.io.InputStream

data class NeoForgeModImpl(
    @JvmField
    val neoforge: ModContainer
) : PlatformMod {
    override val id: String
        get() = neoforge.modInfo.modId
    override val version: String
        get() = neoforge.modInfo.version.toString()
    override val bigShotData: BigShotModData? by lazy {
        findResource(BigShotModData.FILE_NAME)?.use {
            try {
                val data = JsonFormat().read(BigShotModData.CODEC, it.bufferedReader().readText())

                if (id != "big_shot" && neoforge.modInfo.dependencies.none { it.modId == "big_shot" && it.type == IModInfo.DependencyType.REQUIRED }) {
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
        return neoforge.modInfo.owningFile.file.contents[file]?.open()
    }

    override fun toString(): String {
        return neoforge.toString()
    }
}