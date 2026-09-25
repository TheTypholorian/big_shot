package net.typho.big_shot.agent.platform

import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.transform.impl.ClassTweakerTransform
import net.typho.big_shot.common.BigShotModData
import net.typho.data_util.impl.JsonFormat
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.readText
import kotlin.properties.Delegates

interface BigShotPlatform {
    val allMods: List<PlatformMod>
    val loaded: Boolean

    fun getModAt(path: Path): PlatformMod?

    fun loadBigShotMetadata() {
        LOG.info("Loading big shot mod metadata")

        for (mod in allMods) {
            try {
                mod.bigShotData?.let { data ->
                    data.classTweaker?.let {
                        val file = mod.findResource(it) ?: throw FileNotFoundException("Mod '$mod' is missing class tweaker ${data.classTweaker}")
                        val tweaker = ClassTweaker.newInstance()
                        ClassTweakerReader.create(tweaker).read(file.bufferedReader())
                        ClassTweakerTransform.CLASS_TWEAKERS.add(tweaker)
                    }
                }
            } catch (t: Throwable) {
                LOG.error("Error while loading big shot mod metadata for $mod", t)
            }
        }
    }

    companion object {
        var INSTANCE: BigShotPlatform? by Delegates.observable(null) { property, old, new ->
            if (old != null && old != new) {
                throw IllegalStateException("Already loaded big shot on $old but trying to load on $new")
            }
        }
            internal set
    }
}