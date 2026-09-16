package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ClassTransformInfo
import org.gradle.api.artifacts.transform.CacheableTransform

@CacheableTransform
abstract class MinecraftTransformAction : JarTransformAction<JarTransformAction.NoParameters> {
    override fun transformClass(info: ClassTransformInfo) {
    }
}