package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.common.event.EventGraph
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
        val visitor = BigShotAgent.REMAP_EVENTS.resolve().foldRight(newNode as ClassVisitor) { event, visitor ->
            event.event.createVisitor(info, visitor) ?: visitor
        }

        if (visitor !== newNode) {
            info.node.accept(visitor)
            info.node = newNode
        }
    }
}