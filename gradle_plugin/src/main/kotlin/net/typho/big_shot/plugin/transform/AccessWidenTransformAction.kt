package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.data.ct.BuiltinClassTweaker
import org.gradle.api.artifacts.transform.CacheableTransform

@CacheableTransform
abstract class AccessWidenTransformAction : JarTransformAction<JarTransformAction.NoParameters> {
    override fun transformClass(info: ClassTransformInfo) {
        BuiltinClassTweaker.apply(info)
    }
}