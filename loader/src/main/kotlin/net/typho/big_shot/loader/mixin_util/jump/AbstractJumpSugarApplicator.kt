package net.typho.big_shot.loader.mixin_util.jump

import com.llamalad7.mixinextras.injector.StackExtension
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import com.llamalad7.mixinextras.sugar.impl.SugarParameter
import com.llamalad7.mixinextras.sugar.impl.SugarPostProcessingExtension
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicValue
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes
import org.spongepowered.asm.mixin.injection.struct.Target
import org.spongepowered.asm.util.asm.ASM
import org.spongepowered.asm.util.asm.MixinVerifier

abstract class AbstractJumpSugarApplicator(
    info: InjectionInfo,
    parameter: SugarParameter
) : SugarApplicator(info, parameter) {
    companion object {
        @JvmField
        val JUMP_HANDLE_TYPE = Type.getType(JumpHandle::class.java)
    }

    protected abstract val annoName: String
    protected lateinit var jumpTarget: LabelNode

    override fun postProcessingPriority() = 500

    override fun validate(target: Target, node: InjectionNodes.InjectionNode) {
        if (JUMP_HANDLE_TYPE != paramType) {
            throw IllegalStateException("@$annoName sugar has wrong type! Expected ${JUMP_HANDLE_TYPE.className} but got ${paramType.className}")
        }
    }

    override fun inject(
        target: Target,
        node: InjectionNodes.InjectionNode,
        stack: StackExtension
    ) {
        target.insns.insertBefore(node.currentTarget, VarInsnNode(Opcodes.ALOAD, createJumpHandle(target, node, stack)))
    }

    fun createJumpHandle(target: Target, node: InjectionNodes.InjectionNode, stack: StackExtension): Int {
        val handleIndex = target.allocateLocal()
        target.addLocalVariable(handleIndex, "jumpHandle$handleIndex", JUMP_HANDLE_TYPE.descriptor)

        val insns = InsnList()
        insns.add(TypeInsnNode(Opcodes.NEW, JUMP_HANDLE_TYPE.internalName))
        insns.add(InsnNode(Opcodes.DUP))
        insns.add(MethodInsnNode(
            Opcodes.INVOKESPECIAL,
            JUMP_HANDLE_TYPE.internalName,
            "<init>",
            "()V"
        ))
        insns.add(VarInsnNode(Opcodes.ASTORE, handleIndex))

        target.insertBefore(node, insns)

        SugarPostProcessingExtension.enqueuePostProcessing(this) {
            val insns = InsnList()
            val notBroken = LabelNode()

            insns.add(VarInsnNode(Opcodes.ALOAD, handleIndex))
            insns.add(MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                JUMP_HANDLE_TYPE.internalName,
                "hasJumped",
                "()Z"
            ))
            insns.add(JumpInsnNode(Opcodes.IFEQ, notBroken))

            val frames = Analyzer(MixinVerifier(
                ASM.API_VERSION,
                Type.getObjectType(target.classNode.name),
                target.classNode.superName?.let { Type.getObjectType(it) },
                target.classNode.interfaces?.map { Type.getObjectType(it) },
                target.classNode.access and Opcodes.ACC_INTERFACE != 0
            )).analyze(target.classNode.name, target.method)

            val sourceFrame = frames[target.insns.indexOf(node.currentTarget) + 1]
            val targetFrame = frames[target.insns.indexOf(jumpTarget)]

            for (i in sourceFrame.stackSize - 1 downTo targetFrame.stackSize) {
                sourceFrame.getStack(i).type?.let { type ->
                    when (type.size) {
                        1 -> insns.add(InsnNode(Opcodes.POP))
                        2 -> insns.add(InsnNode(Opcodes.POP2))
                        else -> throw AssertionError()
                    }
                }
            }

            repeat(targetFrame.locals) { i ->
                val expected = targetFrame.getLocal(i)
                val actual = sourceFrame.getLocal(i)

                if (expected == BasicValue.UNINITIALIZED_VALUE || actual != BasicValue.UNINITIALIZED_VALUE) {
                    return@repeat
                }

                val type = expected.type ?: return@repeat

                when (type.sort) {
                    Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> {
                        insns.add(InsnNode(Opcodes.ICONST_0))
                        insns.add(VarInsnNode(Opcodes.ISTORE, i))
                    }
                    Type.FLOAT -> {
                        insns.add(InsnNode(Opcodes.FCONST_0))
                        insns.add(VarInsnNode(Opcodes.FSTORE, i))
                    }
                    Type.LONG -> {
                        insns.add(InsnNode(Opcodes.LCONST_0))
                        insns.add(VarInsnNode(Opcodes.LSTORE, i))
                    }
                    Type.DOUBLE -> {
                        insns.add(InsnNode(Opcodes.DCONST_0))
                        insns.add(VarInsnNode(Opcodes.DSTORE, i))
                    }
                    else -> {
                        insns.add(InsnNode(Opcodes.ACONST_NULL))
                        insns.add(VarInsnNode(Opcodes.ASTORE, i))
                    }
                }
            }

            insns.add(JumpInsnNode(Opcodes.GOTO, jumpTarget))
            insns.add(notBroken)

            target.insns.insert(node.currentTarget, insns)
        }

        stack.extra(50)
        return handleIndex
    }
}