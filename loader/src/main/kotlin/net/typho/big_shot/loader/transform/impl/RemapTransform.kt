package net.typho.big_shot.loader.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.remap.CompatClassRemapper
import net.typho.big_shot.loader.BigShotLoader.REMAP_EVENTS
import net.typho.big_shot.loader.mixin.kotlin.KotlinMixinFixer
import net.typho.big_shot.loader.transform.TransformEvent
import net.typho.big_shot.loader.transform.TransformType
import net.typho.big_shot.loader.util.EventGraph
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.ClassNode

object RemapTransform : EventGraph.SelfAware<String, TransformEvent>, TransformEvent {
    override val id: String
        get() = "big_shot:remap"

    override fun postRegister(event: EventGraph<String, TransformEvent>.Event) {
        event.after(KotlinMixinFixer) // we want to remap after kotlin mixins are fixed, since companion objects
    }

    override fun transform(
        type: TransformType,
        info: ClassTransformInfo
    ) {
        val newNode = ClassNode()
        val visitor = REMAP_EVENTS.resolve().foldRight(newNode as ClassVisitor) { event, visitor ->
            val remapper = event.event.createRemapper(info)
            if (remapper == null) visitor else CompatClassRemapper(visitor, remapper)
        }

        if (visitor !== newNode) {
            info.node.accept(visitor)
            info.node = newNode
        }
    }
}