package net.typho.big_shot.loader.mixin_util

import com.llamalad7.mixinextras.injector.StackExtension
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import com.llamalad7.mixinextras.sugar.impl.SugarParameter
import com.llamalad7.mixinextras.sugar.impl.SugarPostProcessingExtension
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicValue
import org.objectweb.asm.util.TraceClassVisitor
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes
import org.spongepowered.asm.mixin.injection.struct.Target
import org.spongepowered.asm.util.asm.ASM
import org.spongepowered.asm.util.asm.MixinVerifier
import java.io.PrintWriter

abstract class AbstractJumpSugarApplicator(
    info: InjectionInfo,
    parameter: SugarParameter
) : SugarApplicator(info, parameter) {
    companion object {
        @JvmField
        val JUMP_HANDLE_TYPE = Type.getType(JumpHandle::class.java)
        val JUMP_HANDLE_IMPL_TYPE = Type.getType(JumpHandle.Impl::class.java)
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
        //target.classNode.accept(TraceClassVisitor(PrintWriter(System.out)))
    }

    fun createJumpHandle(target: Target, node: InjectionNodes.InjectionNode, stack: StackExtension): Int {
        val frames = Analyzer(MixinVerifier(
            ASM.API_VERSION,
            Type.getObjectType(target.classNode.name),
            target.classNode.superName?.let { Type.getObjectType(it) },
            target.classNode.interfaces?.map { Type.getObjectType(it) },
            target.classNode.access and Opcodes.ACC_INTERFACE != 0
        )).analyze(target.classNode.name, target.method)

        val sourceFrame = frames[target.insns.indexOf(node.currentTarget) + 1]
        val targetFrame = frames[target.insns.indexOf(jumpTarget)]

        val handleIndex = target.allocateLocal()
        target.addLocalVariable(handleIndex, "jumpHandle$handleIndex", JUMP_HANDLE_IMPL_TYPE.descriptor)

        val insns = InsnList()
        insns.add(TypeInsnNode(Opcodes.NEW, JUMP_HANDLE_IMPL_TYPE.internalName))
        insns.add(InsnNode(Opcodes.DUP))
        insns.add(LdcInsnNode(0))
        insns.add(LdcInsnNode(targetFrame.stackSize))
        insns.add(MethodInsnNode(
            Opcodes.INVOKESPECIAL,
            JUMP_HANDLE_IMPL_TYPE.internalName,
            "<init>",
            "(II)V"
        ))
        insns.add(VarInsnNode(Opcodes.ASTORE, handleIndex))

        target.insertBefore(node, insns)

        SugarPostProcessingExtension.enqueuePostProcessing(this) {
            val insns = InsnList()
            val notBroken = LabelNode()

            insns.add(VarInsnNode(Opcodes.ALOAD, handleIndex))
            insns.add(MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                JUMP_HANDLE_IMPL_TYPE.internalName,
                "hasJumped",
                "()Z"
            ))
            insns.add(JumpInsnNode(Opcodes.IFEQ, notBroken))

            println("source $sourceFrame ${sourceFrame.stackSize}")
            println("target $targetFrame ${targetFrame.stackSize}")

            repeat(sourceFrame.stackSize) { i ->
                val stack = sourceFrame.getStack(sourceFrame.stackSize - 1 - i);

                if (stack != BasicValue.UNINITIALIZED_VALUE) {
                    println("pop ${stack.type}")
                    when (stack.type.size) {
                        1 -> insns.add(InsnNode(Opcodes.POP))
                        2 -> insns.add(InsnNode(Opcodes.POP2))
                        else -> throw AssertionError()
                    }
                }
            }

            var stackIndex = 0
            repeat(targetFrame.stackSize) { i ->
                val expected = targetFrame.getStack(i)
                val type = expected.type!!

                println("stack difference $type $i")

                insns.add(VarInsnNode(Opcodes.ALOAD, handleIndex))
                insns.add(LdcInsnNode(stackIndex++))
                when (type.sort) {
                    Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> insns.add(MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        JUMP_HANDLE_IMPL_TYPE.internalName,
                        "stackInt",
                        "(I)I"
                    ))
                    Type.LONG -> insns.add(MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        JUMP_HANDLE_IMPL_TYPE.internalName,
                        "stackLong",
                        "(I)J"
                    ))
                    Type.FLOAT -> insns.add(MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        JUMP_HANDLE_IMPL_TYPE.internalName,
                        "stackFloat",
                        "(I)F"
                    ))
                    Type.DOUBLE -> insns.add(MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        JUMP_HANDLE_IMPL_TYPE.internalName,
                        "stackDouble",
                        "(I)D"
                    ))
                    else -> {
                        insns.add(MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            JUMP_HANDLE_IMPL_TYPE.internalName,
                            "stackObject",
                            "(I)Ljava/lang/Object;"
                        ))
                        insns.add(TypeInsnNode(
                            Opcodes.CHECKCAST,
                            type.internalName
                        ))
                    }
                }
            }

            insns.add(JumpInsnNode(Opcodes.GOTO, jumpTarget))

            insns.add(notBroken)
            target.insns.insert(node.currentTarget, insns)
        }

        stack.extra(50) // TODO
        return handleIndex
    }
}