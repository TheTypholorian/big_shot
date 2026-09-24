package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ClassTransformInfo
import org.gradle.api.artifacts.transform.CacheableTransform

@CacheableTransform
abstract class MinecraftTransformAction : JarTransformAction.Transformer, JarTransformAction<JarTransformAction.NoParameters> {
    override fun createTransformer(): JarTransformAction.Transformer {
        return this
    }

    override fun transformClass(info: ClassTransformInfo) {
    }
}