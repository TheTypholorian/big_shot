package net.typho.big_shot.agent.platform

import net.typho.big_shot.agent.PlatformMod
import java.nio.file.Path
import kotlin.properties.Delegates

interface BigShotPlatform {
    fun getModAt(path: Path): PlatformMod?

    companion object {
        var INSTANCE: BigShotPlatform? by Delegates.observable(null) { property, old, new ->
            if (old != null && old != new) {
                throw IllegalStateException("Already loaded big shot on $old but trying to load on $new")
            }
        }
            internal set
    }
}