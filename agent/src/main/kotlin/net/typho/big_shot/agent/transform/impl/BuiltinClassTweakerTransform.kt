package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.util.ct.BuiltinClassTweaker
import net.typho.big_shot.util.event.EventGraph

object BuiltinClassTweakerTransform : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:early_access_widener"

    override fun postRegister(event: EventGraph<String, *>.Event) {
        event.after(RemapTransform)
    }

    override fun transform(
        type: TransformSource,
        info: ClassTransformInfo
    ) {
        BuiltinClassTweaker.apply(info)
    }
}