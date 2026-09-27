package net.typho.big_shot.api.platform.fabric

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.typho.big_shot.agent.platform.BigShotPlatform
import net.typho.big_shot.agent.platform.BigShotPlatform.Companion.loadAll

class FabricPreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        println("Mods for class: " + BigShotPlatform.INSTANCE?.getModsThatHaveClass("net.typho.test_mod.TestModEarly"))
        BigShotPlatform.INSTANCE!!.loadModEntrypoint("early", Any::class.java).loadAll().forEach { (mod, entrypoints) ->
            println("Loaded entrypoints $entrypoints for mod $mod")
        }
    }
}