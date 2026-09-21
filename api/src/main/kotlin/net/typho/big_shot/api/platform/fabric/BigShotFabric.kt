package net.typho.big_shot.api.platform.fabric

import net.fabricmc.loader.impl.game.GameProvider
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.typho.big_shot.api.BigShot
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.asm.mixin.Mixins

@ApiStatus.Internal
@Suppress("unused")
object BigShotFabric {
    @JvmStatic
    fun clinit() {
        println("loaded into a bright future with mucho shenanigans to come")
    }

    @JvmStatic
    fun loadGameProvider(provider: GameProvider) {
        println("game provider $provider, name ${provider.gameName} and version ${provider.rawGameVersion}")
    }

    @JvmStatic
    fun registerMixins() {
        println("registering mixins")
        Mixins.addConfiguration("big_shot.mixins.json")
    }

    @JvmStatic
    fun finishModLoading() {
        FabricLauncherBase.getLauncher().addToClassPath(BigShot.API_PATH)
    }
}