package net.typho.big_shot.loader.mixin.jumps

import com.llamalad7.mixinextras.injector.StackExtension
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import com.llamalad7.mixinextras.sugar.impl.SugarParameter
import com.llamalad7.mixinextras.sugar.impl.SugarPostProcessingExtension
import com.llamalad7.mixinextras.utils.CompatibilityHelper
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicValue
import org.objectweb.asm.tree.analysis.Frame
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator
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
        @JvmField
        val JUMP_HANDLE_IMPL_TYPE = Type.getType(JumpHandle.Impl::class.java)

        @JvmField
        val JUMP_HANDLE_COMPLEX_TYPE = Type.getType(JumpHandle.Complex::class.java)
        @JvmField
        val JUMP_HANDLE_COMPLEX_IMPL_TYPE = Type.getType(JumpHandle.ComplexImpl::class.java)
    }

    protected lateinit var jumpTarget: AbstractInsnNode
    protected var localsToModify: List<Pair<Type, LocalVariableDiscriminator>> = listOf()
    protected lateinit var sourceFrame: Frame<BasicValue>
    protected lateinit var targetFrame: Frame<BasicValue>

    override fun postProcessingPriority() = 1000

    protected fun analyze(target: Target): Array<Frame<BasicValue>> {
        return Analyzer(MixinVerifier(
            ASM.API_VERSION,
            Type.getObjectType(target.classNode.name),
            target.classNode.superName?.let { Type.getObjectType(it) },
            target.classNode.interfaces?.map { Type.getObjectType(it) },
            target.classNode.access and Opcodes.ACC_INTERFACE != 0
        )).analyze(target.classNode.name, target.method)
    }

    protected fun loadFrames(frames: Array<Frame<BasicValue>>, target: Target, node: InjectionNodes.InjectionNode) {
        sourceFrame = frames[target.insns.indexOf(node.currentTarget)]
        targetFrame = frames[target.insns.indexOf(jumpTarget)]
    }

    override fun inject(
        target: Target,
        node: InjectionNodes.InjectionNode,
        stack: StackExtension
    ) {
        val complex = paramType == JUMP_HANDLE_COMPLEX_TYPE
        val handleImplType = if (complex) JUMP_HANDLE_COMPLEX_IMPL_TYPE else JUMP_HANDLE_IMPL_TYPE

        if (localsToModify.isNotEmpty() && !complex) {
            throw IllegalStateException("Specified locals to modify in jump annotation but doesn't take a complex jump handle")
        }

        val jumpTarget = jumpTarget as? LabelNode ?: LabelNode().also { target.method.instructions.insertBefore(jumpTarget, it) }

        val handleIndex = target.allocateLocal()
        target.addLocalVariable(handleIndex, "jumpHandle$handleIndex", handleImplType.descriptor)

        stack.extra(2)
        val insns = InsnList()
        insns.add(TypeInsnNode(Opcodes.NEW, handleImplType.internalName))
        insns.add(InsnNode(Opcodes.DUP))

        if (complex) {
            insns.add(LdcInsnNode(localsToModify.size))
            insns.add(LdcInsnNode(targetFrame.stackSize))
            insns.add(MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                handleImplType.internalName,
                "<init>",
                "(II)V"
            ))
        } else {
            insns.add(MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                handleImplType.internalName,
                "<init>",
                "()V"
            ))
        }

        insns.add(VarInsnNode(Opcodes.ASTORE, handleIndex))
        target.insertBefore(node, insns)

        SugarPostProcessingExtension.enqueuePostProcessing(this) {
            val insns = InsnList()
            val notJumped = LabelNode()

            stack.extra(1)
            insns.add(VarInsnNode(Opcodes.ALOAD, handleIndex))
            insns.add(MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                handleImplType.internalName,
                "hasJumped",
                "()Z"
            ))
            insns.add(JumpInsnNode(Opcodes.IFEQ, notJumped))

            repeat(sourceFrame.stackSize) { i ->
                val stack = sourceFrame.getStack(sourceFrame.stackSize - 1 - i)

                if (stack != BasicValue.UNINITIALIZED_VALUE) {
                    when (stack.type.size) {
                        1 -> insns.add(InsnNode(Opcodes.POP))
                        2 -> insns.add(InsnNode(Opcodes.POP2))
                        else -> throw AssertionError()
                    }
                }
            }

            if (complex) {
                if (localsToModify.isNotEmpty()) {
                    val contexts = mutableMapOf<Pair<Type, Boolean>, LocalVariableDiscriminator.Context>()
                    var localIndex = 0
                    val indices = mutableSetOf<Int>()

                    for ((type, local) in localsToModify) {
                        val context = contexts.computeIfAbsent(type to local.isArgsOnly) {
                            CompatibilityHelper.makeLvtContext(info, it.first, it.second, target, jumpTarget)
                        }
                        val id = local.findLocal(context)

                        if (!indices.add(id)) {
                            throw IllegalStateException("Specified the same local to modify more than once in jump")
                        }

                        stack.extra(1)
                        insns.add(VarInsnNode(Opcodes.ALOAD, handleIndex))
                        insns.add(LdcInsnNode(localIndex++))
                        when (type.sort) {
                            Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> {
                                insns.add(MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    handleImplType.internalName,
                                    "localInt",
                                    "(I)I"
                                ))
                                insns.add(VarInsnNode(Opcodes.ISTORE, id))
                            }
                            Type.LONG -> {
                                insns.add(MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    handleImplType.internalName,
                                    "localLong",
                                    "(I)J"
                                ))
                                insns.add(VarInsnNode(Opcodes.LSTORE, id))
                            }
                            Type.FLOAT -> {
                                insns.add(MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    handleImplType.internalName,
                                    "localFloat",
                                    "(I)F"
                                ))
                                insns.add(VarInsnNode(Opcodes.FSTORE, id))
                            }
                            Type.DOUBLE -> {
                                insns.add(MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    handleImplType.internalName,
                                    "localDouble",
                                    "(I)D"
                                ))
                                insns.add(VarInsnNode(Opcodes.DSTORE, id))
                            }
                            else -> {
                                insns.add(MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    handleImplType.internalName,
                                    "localObject",
                                    "(I)Ljava/lang/Object;"
                                ))
                                insns.add(TypeInsnNode(
                                    Opcodes.CHECKCAST,
                                    type.internalName
                                ))
                                insns.add(VarInsnNode(Opcodes.ASTORE, id))
                            }
                        }
                    }
                }

                var stackIndex = 0
                repeat(targetFrame.stackSize) { i ->
                    val expected = targetFrame.getStack(i)
                    val type = expected.type!!

                    stack.extra(1)
                    insns.add(VarInsnNode(Opcodes.ALOAD, handleIndex))
                    insns.add(LdcInsnNode(stackIndex++))
                    when (type.sort) {
                        Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> insns.add(MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            handleImplType.internalName,
                            "stackInt",
                            "(I)I"
                        ))
                        Type.LONG -> insns.add(MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            handleImplType.internalName,
                            "stackLong",
                            "(I)J"
                        ))
                        Type.FLOAT -> insns.add(MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            handleImplType.internalName,
                            "stackFloat",
                            "(I)F"
                        ))
                        Type.DOUBLE -> insns.add(MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            handleImplType.internalName,
                            "stackDouble",
                            "(I)D"
                        ))
                        else -> {
                            insns.add(MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                handleImplType.internalName,
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
            }

            insns.add(JumpInsnNode(Opcodes.GOTO, jumpTarget))

            insns.add(notJumped)
            target.insns.insert(node.currentTarget, insns)
        }

        target.insns.insertBefore(node.currentTarget, VarInsnNode(Opcodes.ALOAD, handleIndex))
        //target.classNode.accept(TraceClassVisitor(PrintWriter(System.out)))
    }
}