package net.typho.big_shot.agent.transform

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.common.event.EventGraph

fun interface TransformEvent {
    fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    )

    /**
     * Injected into [org.spongepowered.asm.mixin.transformer.MixinInfo#loadMixinClass].
     *
     * Note that [transform] will already have been run on this class.
     */
    fun transformMixin(info: ClassTransformInfo) {
    }

    /**
     * Injected into [org.spongepowered.asm.mixin.transformer.ClassInfo#loadMixinClass].
     *
     * Note that [transform] will already have been run on this class.
     */
    fun transformClassInfo(info: ClassTransformInfo) {
    }

    abstract class KnownTargets(
        @JvmField
        val names: Set<String>
    ) : EventGraph.SelfAware<String>, TransformEvent {
        constructor(vararg names: String) : this(setOf(*names))

        init {
            if (names.isEmpty()) {
                throw IllegalArgumentException()
            }
        }

        override fun postRegister(event: EventGraph<String, *>.Event) {
            names.forEach { name ->
                if (BigShotAgent.INSTRUMENTATION.allLoadedClasses.any { it.name.replace('.', '/') == name }) {
                    throw AssertionError("Registered transform for $name but it has already been loaded")
                }

                // TODO check MixinInfo and ClassInfo as well
            }
        }

        final override fun transform(mod: PlatformMod?, info: ClassTransformInfo) {
            if (names.contains(info.className)) {
                transformImpl(mod, info)
            }
        }

        protected abstract fun transformImpl(mod: PlatformMod?, info: ClassTransformInfo)
    }
}