package net.typho.big_shot.api.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.data.ct.BuiltinClassTweaker
import net.typho.big_shot.api.transform.TransformEvent
import net.typho.big_shot.api.transform.TransformSource
import net.typho.big_shot.api.util.EventGraph

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