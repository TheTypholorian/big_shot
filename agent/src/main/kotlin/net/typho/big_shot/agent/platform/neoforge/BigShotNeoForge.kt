package net.typho.big_shot.agent.platform.neoforge

import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.platform.BigShotPlatform
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path

@ApiStatus.Internal
@Suppress("unused")
object BigShotNeoForge : BigShotPlatform, EventGraph.SelfAware<String>, TransformEvent {
    override val allMods: List<PlatformMod>
        get() = ModList.get().sortedMods.map { NeoForgeModImpl(it) }
    override val id: String
        get() = "big_shot:platform/neoforge"
    override var loaded = false
        private set
    override val classLoader: ClassLoader
        get() = FMLLoader.getCurrent().currentClassLoader ?: Thread.currentThread().contextClassLoader

    init {
        LOG = NeoForgeLogImpl
        LOG.info("Loading big shot on neoforge")
        BigShotAgent.TRANSFORM_EVENTS.register(this)
    }

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
    }

    override fun getModAt(path: Path): PlatformMod? {
        if (!loaded) {
            return null
        }

        return ModList.get().sortedMods.firstOrNull { it.modInfo.owningFile.file.filePath == path }?.let { NeoForgeModImpl(it) }
    }
}