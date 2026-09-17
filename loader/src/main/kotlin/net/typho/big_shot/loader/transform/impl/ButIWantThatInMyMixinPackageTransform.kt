package net.typho.big_shot.loader.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.loader.constant.TransformEventNames
import net.typho.big_shot.loader.transform.TransformEvent
import net.typho.big_shot.loader.transform.TransformType
import net.typho.big_shot.loader.util.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

object ButIWantThatInMyMixinPackageTransform : EventGraph.SelfAware<String, TransformEvent>, TransformEvent {
    override val id: String
        get() = TransformEventNames.BUT_I_WANT_THAT_IN_MY_MIXIN_PACKAGE

    override fun transform(
        type: TransformType,
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
                            "net/typho/big_shot/loader/BigShotLoader",
                            "bypassMixinPackageRestriction",
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
}