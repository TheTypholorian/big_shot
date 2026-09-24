package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

object InjectMixinTransforms : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:mixin_transforms"

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "org/spongepowered/asm/mixin/transformer/MixinInfo" -> {
                info.markChanged()
                info.computeMaxStacks()

                MethodPointer.method()
                    .name("loadMixinClass")
                    .desc("(Ljava/lang/String;)Lorg/objectweb/asm/tree/ClassNode;")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insertBefore(
                            InsnPointer.simple()
                                .opcode(Opcodes.ARETURN)
                                .lastOrdinal()
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(InsnNode(Opcodes.DUP))
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/agent/transform/impl/InjectMixinTransforms",
                                        "transformMixinInfo",
                                        "(Lorg/objectweb/asm/tree/ClassNode;)V"
                                    )
                                )
                            }
                        )
                    }
            }

            "org/spongepowered/asm/mixin/transformer/ClassInfo" -> {
                info.markChanged()
                info.computeMaxStacks()

                MethodPointer.method()
                    .name("<init>")
                    .desc("(Lorg/objectweb/asm/tree/ClassNode;)V")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(
                            InsnPointer.methodCall()
                                .owner("java/lang/Object")
                                .name("<init>")
                                .desc("()V")
                                .ordinal(0)
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(VarInsnNode(Opcodes.ALOAD, 1))
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/agent/transform/impl/InjectMixinTransforms",
                                        "transformClassInfo",
                                        "(Lorg/objectweb/asm/tree/ClassNode;)V"
                                    )
                                )
                            }
                        )
                    }
            }
        }
    }

    @Suppress("unused")
    @JvmStatic
    fun transformMixinInfo(node: ClassNode) {
        try {
            val info = ClassTransformInfo.Wrapper(node)

            BigShotAgent.TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transformMixin(info)
            }

            info.checkErrors()

            if (info.changed) {
                BigShotAgent.debugSaveClass(node)
            }
        } catch (t: Throwable) {
            throw ClassVisitException("Error transforming mixin class ${node.name}\nTransform event graph:\n${BigShotAgent.TRANSFORM_EVENTS}", t)
        }
    }

    @Suppress("unused")
    @JvmStatic
    fun transformClassInfo(node: ClassNode) {
        try {
            val info = ClassTransformInfo.Wrapper(node)

            BigShotAgent.TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transformClassInfo(info)
            }

            info.checkErrors()

            if (info.changed) {
                BigShotAgent.debugSaveClass(node)
            }
        } catch (t: Throwable) {
            throw ClassVisitException("Error transforming mixin class ${node.name}\nTransform event graph:\n${BigShotAgent.TRANSFORM_EVENTS}", t)
        }
    }
}