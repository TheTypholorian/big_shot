package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.api.metadata.ModOrigin
import net.typho.big_shot.agent.AgentLoadedCheck
import java.nio.file.Path

class FabricPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        fun getModPaths(mod: ModOrigin): List<Path> = when (mod.kind) {
            ModOrigin.Kind.PATH -> mod.paths
            ModOrigin.Kind.NESTED -> getModPaths(FabricLoader.getInstance().getModContainer(mod.parentModId).orElseThrow().origin)
            else -> listOf()
        }

        AgentLoadedCheck.check(getModPaths(FabricLoader.getInstance().getModContainer("big_shot_agent_check").orElseThrow().origin))
    }
}