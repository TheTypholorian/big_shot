package net.typho.big_shot.loader.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.data.ct.BuiltinClassTweaker
import net.typho.big_shot.loader.constant.TransformEventNames
import net.typho.big_shot.loader.transform.TransformEvent
import net.typho.big_shot.loader.transform.TransformType
import net.typho.big_shot.loader.util.EventGraph

object BuiltinClassTweakerTransform : EventGraph.SelfAware<String, TransformEvent>, TransformEvent {
    override val id: String
        get() = TransformEventNames.EARLY_ACCESS_WIDENER

    override fun postRegister(event: EventGraph<String, TransformEvent>.Event) {
        event.after(RemapTransform)
    }

    override fun transform(
        type: TransformType,
        info: ClassTransformInfo
    ) {
        BuiltinClassTweaker.apply(info)
    }
}