package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.api.metadata.ModOrigin
import net.typho.big_shot.agent.AgentNotLoadedError
import kotlin.jvm.optionals.getOrNull

class FabricPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        AgentNotLoadedError.error(FabricLoader.getInstance().getModContainer("big_shot_agent_check").getOrNull()?.origin?.let { origin ->
            if (origin.kind == ModOrigin.Kind.PATH) origin.paths else null
        } ?: listOf())
    }
}