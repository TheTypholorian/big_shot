package net.typho.big_shot.loader.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.loader.transform.TransformEvent
import net.typho.big_shot.loader.transform.TransformSource
import net.typho.big_shot.loader.util.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

object InjectMixinUtilsTransform : EventGraph.SelfAware<String, TransformEvent>, TransformEvent {
    override val id: String
        get() = "big_shot:inject_mixin_utils"

    override fun transform(
        type: TransformSource,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "com/llamalad7/mixinextras/sugar/impl/SugarApplicator" -> {
                MethodPointer.method().name("<clinit>").findOrThrow(info.node) { method ->
                    InsnPointer.methodCallStatic().owner("java/util/Arrays").name("asList").ordinal(0).findOrThrow(method.instructions) { insn ->
                        method.instructions.insert(insn, MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/typho/big_shot/loader/BigShotLoader",
                            "registerExtraMixinSugars",
                            "(Ljava/util/List;)Ljava/util/List;"
                        ))
                    }
                }
                info.markChanged()
            }
            "org/spongepowered/asm/mixin/injection/InjectionPoint" -> {
                MethodPointer.method().name("<clinit>").findOrThrow(info.node) { method ->
                    InsnPointer.simple().opcode(Opcodes.RETURN).findOrThrow(method.instructions) { insn ->
                        method.instructions.insertBefore(insn, MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/typho/big_shot/loader/BigShotLoader",
                            "registerInjectionPoints",
                            "()V"
                        ))
                    }
                }
                info.markChanged()
                info.computeFrames()
            }
        }
    }
}