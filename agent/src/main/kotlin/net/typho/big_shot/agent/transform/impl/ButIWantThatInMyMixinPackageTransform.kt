package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.spongepowered.asm.mixin.transformer.ClassInfo

object ButIWantThatInMyMixinPackageTransform : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:but_i_want_that_in_my_mixin_package"

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "org/spongepowered/asm/mixin/transformer/MixinProcessor" -> {
                MethodPointer.method().name("applyMixins").findOrThrow(info.node) { method ->
                    val hasSuperClass = InsnPointer.methodCall()
                        .owner("org/spongepowered/asm/mixin/transformer/ClassInfo")
                        .name("hasSuperClass")
                        .desc("(Ljava/lang/Class;)Z")
                        .ordinal(0)
                        .findOrThrow(method.instructions)
                    val insn = InsnPointer.localOperation()
                        .lastOrdinal()
                        .before(hasSuperClass)
                        .findOrThrow(method.instructions)

                    method.instructions.insertBefore(insn, InsnList().apply {
                        val label = InsnPointer.jump()
                            .opcode(Opcodes.IFNE)
                            .ordinal(0)
                            .after(hasSuperClass)
                            .findOrThrow(method.instructions)
                            .label

                        add(VarInsnNode(Opcodes.ALOAD, 9))
                        add(MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/typho/big_shot/agent/transform/impl/ButIWantThatInMyMixinPackageTransform",
                            "test",
                            "(Lorg/spongepowered/asm/mixin/transformer/ClassInfo;)Z"
                        ))
                        add(JumpInsnNode(Opcodes.IFNE, label))
                    })
                }
                info.markChanged()
                info.computeFrames()
            }
        }
    }

    @Suppress("unused")
    @JvmStatic
    fun test(info: ClassInfo) = info.name.startsWith("net/typho/big_shot")
}