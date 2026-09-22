package net.typho.big_shot.agent.mixin.jumps

import com.llamalad7.mixinextras.sugar.impl.SugarParameter
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.AnnotationNode
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes
import org.spongepowered.asm.mixin.injection.struct.Target
import org.spongepowered.asm.mixin.struct.AnnotatedMethodInfo
import org.spongepowered.asm.util.Annotations

class JumpSugarApplicator(
    info: InjectionInfo,
    parameter: SugarParameter
) : AbstractJumpSugarApplicator(info, parameter) {
    override fun validate(target: Target, node: InjectionNodes.InjectionNode) {
        if (!(paramType == JUMP_HANDLE_TYPE || paramType == JUMP_HANDLE_COMPLEX_TYPE)) {
            throw IllegalStateException("@Jump sugar has wrong type! Expected ${JUMP_HANDLE_TYPE.className} but got ${paramType.className}")
        }
    }

    override fun prepare(
        target: Target,
        node: InjectionNodes.InjectionNode
    ) {
        val frames = analyze(target)

        val injectionPoint = InjectionPoint.parse(AnnotatedMethodInfo(info.mixin, target.method, sugar), Annotations.getValue<AnnotationNode>(sugar, "value"))
        val targets = mutableListOf<AbstractInsnNode>()
        injectionPoint.find(target.method.desc, target.method.instructions, targets)
        var targetNode = targets.singleOrNull() ?: throw IllegalStateException("@Jump must specify exactly one target, got ${targets.size}")

        localsToModify = Annotations.getValue<AnnotationNode>(sugar, "localsToModify", false).map { Annotations.getValue(it, "type", Type.VOID_TYPE) to LocalVariableDiscriminator.parse(it) }

        if (Annotations.getValue<Boolean?>(sugar, "shiftBeforeStack") == true) {
            var index = target.method.instructions.indexOf(targetNode)

            while (frames[index].stackSize > 0) {
                index--
                targetNode = targetNode.previous
            }
        }

        jumpTarget = targetNode
        loadFrames(frames, target, node)
    }
}