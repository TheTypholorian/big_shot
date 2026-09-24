package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.api.metadata.ModOrigin
import net.typho.big_shot.agent.AgentLoadedCheck
import java.nio.file.Path

class FabricPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        AgentLoadedCheck.check(FabricLoader.getInstance().getModContainer("big_shot_agent_check").orElseThrow().rootPaths)
    }
}