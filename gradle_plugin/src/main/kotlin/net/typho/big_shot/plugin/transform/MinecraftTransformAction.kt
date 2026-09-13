package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ClassTransformInfo
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class MinecraftTransformAction : JarTransformAction<JarTransformAction.NoParameters> {
    override fun transformClass(info: ClassTransformInfo) {
    }
}