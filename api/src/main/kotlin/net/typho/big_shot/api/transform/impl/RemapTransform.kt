package net.typho.big_shot.api.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.api.BigShot.REMAP_EVENTS
import net.typho.big_shot.api.mixin.kotlin.KotlinMixinFixer
import net.typho.big_shot.api.transform.TransformEvent
import net.typho.big_shot.api.transform.TransformSource
import net.typho.big_shot.api.util.EventGraph
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.ClassNode

object RemapTransform : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:remap"

    override fun postRegister(event: EventGraph<String, *>.Event) {
        event.after(KotlinMixinFixer) // we want to remap after kotlin mixins are fixed, since companion objects
    }

    override fun transform(
        type: TransformSource,
        info: ClassTransformInfo
    ) {
        val newNode = ClassNode()
        val visitor = REMAP_EVENTS.resolve().foldRight(newNode as ClassVisitor) { event, visitor ->
            event.event.createVisitor(info, visitor) ?: visitor
        }

        if (visitor !== newNode) {
            info.node.accept(visitor)
            info.node = newNode
        }
    }
}