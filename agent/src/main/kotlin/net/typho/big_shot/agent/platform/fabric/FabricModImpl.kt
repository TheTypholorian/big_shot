package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.ModContainer
import net.typho.big_shot.agent.PlatformMod

data class FabricModImpl(
    @JvmField
    val fabric: ModContainer
) : PlatformMod {
    override val id: String
        get() = fabric.metadata.id
    override val version: String
        get() = fabric.metadata.version.friendlyString

    override fun toString(): String {
        return fabric.toString()
    }
}