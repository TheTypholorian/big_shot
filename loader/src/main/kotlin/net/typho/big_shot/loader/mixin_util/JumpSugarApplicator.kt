package net.typho.big_shot.loader.mixin_util

import com.llamalad7.mixinextras.sugar.impl.SugarParameter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes
import org.spongepowered.asm.mixin.injection.struct.Target
import org.spongepowered.asm.mixin.struct.AnnotatedMethodInfo
import org.spongepowered.asm.util.Annotations
import org.spongepowered.asm.util.asm.ASM
import org.spongepowered.asm.util.asm.MixinVerifier

class JumpSugarApplicator(
    info: InjectionInfo,
    parameter: SugarParameter
) : AbstractJumpSugarApplicator(info, parameter) {
    override val annoName: String
        get() = "Jump"

    override fun prepare(
        target: Target,
        node: InjectionNodes.InjectionNode
    ) {
        val injectionPoint = InjectionPoint.parse(AnnotatedMethodInfo(info.mixin, target.method, sugar), Annotations.getValue<AnnotationNode>(sugar, "value"))
        val targets = mutableListOf<AbstractInsnNode>()
        injectionPoint.find(target.method.desc, target.method.instructions, targets)

        val targetNode = targets.singleOrNull() ?: throw IllegalStateException("@Jump must specify exactly one target, got ${targets.size}")
        jumpTarget = targetNode as? LabelNode ?: LabelNode().also { target.method.instructions.insertBefore(targetNode, it) }
    }
}