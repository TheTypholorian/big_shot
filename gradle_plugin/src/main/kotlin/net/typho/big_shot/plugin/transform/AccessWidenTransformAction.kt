package net.typho.big_shot.plugin.transform

import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.common.ct.ClassTweakers
import net.typho.big_shot.common.ct.ClassTweakers.apply
import org.gradle.api.artifacts.transform.CacheableTransform
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File
import java.nio.file.Path
import kotlin.io.path.bufferedReader

@CacheableTransform
abstract class AccessWidenTransformAction : JarTransformAction<AccessWidenTransformAction.Parameters> {
    override fun createTransformer(): JarTransformAction.Transformer {
        val classTweakers = parameters.classTweakers.get().map {
            val tweaker = ClassTweaker.newInstance()
            ClassTweakerReader.create(tweaker).read(it.bufferedReader())
            tweaker
        }
        return object : JarTransformAction.Transformer {
            override fun transformClass(info: ClassTransformInfo) {
                ClassTweakers.EARLY.apply(info)
                classTweakers.forEach { it.apply(info) }
            }
        }
    }

    interface Parameters : TransformParameters {
        @get:InputFiles
        @get:PathSensitive(PathSensitivity.NONE)
        val classTweakers: ListProperty<File>
    }
}