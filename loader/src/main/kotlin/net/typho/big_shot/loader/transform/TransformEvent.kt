package net.typho.big_shot.loader.transform

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.loader.BigShotLoader
import net.typho.big_shot.loader.util.EventGraph

fun interface TransformEvent {
    fun transform(
        type: TransformSource,
        info: ClassTransformInfo
    )

    abstract class ForSingleClass(
        @JvmField
        vararg val names: String
    ) : EventGraph.SelfAware<String, TransformEvent>, TransformEvent {
        init {
            if (names.isEmpty()) {
                throw IllegalArgumentException()
            }
        }

        override fun postRegister(event: EventGraph<String, TransformEvent>.Event) {
            names.forEach { name ->
                if (BigShotLoader.INSTRUMENTATION.allLoadedClasses.any { it.name.replace('.', '/') == name }) {
                    throw AssertionError("Registered transform for $name but it has already been loaded")
                }

                // TODO check MixinInfo and ClassInfo as well
            }
        }

        override fun transform(type: TransformSource, info: ClassTransformInfo) {
            if (names.contains(info.className)) {
                transformImpl(type, info)
            }
        }

        protected abstract fun transformImpl(type: TransformSource, info: ClassTransformInfo)
    }
}