package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

object InjectMixinUtilsTransform : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:mixin_utils"

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "com/llamalad7/mixinextras/service/MixinExtrasServiceImpl" -> {
                MethodPointer.method().name("initialize").findOrThrow(info.node) { method ->
                    InsnPointer.simple().opcode(Opcodes.RETURN).find(method.instructions).forEach { insn ->
                        method.instructions.insertBefore(insn, MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/typho/big_shot/api/mixin/BigShotMixinUtils",
                            "register",
                            "()V"
                        ))
                    }
                }
                info.markChanged()
            }
        }
    }
}