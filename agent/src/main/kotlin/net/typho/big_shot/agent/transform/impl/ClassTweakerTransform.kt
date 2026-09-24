package net.typho.big_shot.agent.transform.impl

import net.fabricmc.classtweaker.api.ClassTweaker
import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.ct.ClassTweakers
import net.typho.big_shot.common.ct.ClassTweakers.apply
import net.typho.big_shot.common.event.EventGraph

object ClassTweakerTransform : EventGraph.SelfAware<String>, TransformEvent {
    @JvmField
    val CLASS_TWEAKERS = mutableListOf<ClassTweaker>(ClassTweakers.EARLY)
    override val id: String
        get() = "big_shot:class_tweaker"

    override fun postRegister(event: EventGraph<String, *>.Event) {
        event.after(RemapTransform)
    }

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        CLASS_TWEAKERS.forEach { it.apply(info) }
    }
}