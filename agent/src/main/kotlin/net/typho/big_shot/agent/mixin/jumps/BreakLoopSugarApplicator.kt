package net.typho.big_shot.agent.mixin.jumps

import com.llamalad7.mixinextras.sugar.impl.SugarParameter
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes
import org.spongepowered.asm.mixin.injection.struct.Target
import org.spongepowered.asm.util.Annotations

class BreakLoopSugarApplicator(
    info: InjectionInfo,
    parameter: SugarParameter
) : AbstractJumpSugarApplicator(info, parameter) {
    private data class Loop(
        @JvmField
        val startLabel: LabelNode,
        @JvmField
        var endLabel: LabelNode? = null
    )

    override fun validate(target: Target, node: InjectionNodes.InjectionNode) {
        if (paramType != JUMP_HANDLE_TYPE) {
            throw IllegalStateException("@BreakLoop sugar has wrong type! Expected ${JUMP_HANDLE_TYPE.className} but got ${paramType.className}")
        }
    }

    override fun prepare(
        target: Target,
        node: InjectionNodes.InjectionNode
    ) {
        val passedLabels = mutableListOf<LabelNode>()
        val loops = mutableListOf<Loop>()

        target.insns.forEach { node ->
            if (node is LabelNode) {
                passedLabels.add(node)

                for (loop in loops) {
                    if (loop.endLabel == null) {
                        loop.endLabel = node
                    }
                }
            } else if (node is JumpInsnNode) {
                if (passedLabels.contains(node.label)) {
                    for (loop in loops) {
                        if (loop.startLabel == node.label) {
                            loop.endLabel = null
                            return@forEach
                        }
                    }

                    loops.add(Loop(node.label))
                }
            }
        }

        val depth = Annotations.getValue(sugar, "depth", -1)
        val targetIndex = target.insns.indexOf(node.currentTarget)

        if (targetIndex == -1) {
            throw IllegalStateException("Current target index is out of bounds")
        }

        val loopStack = loops.filter { loop ->
            target.insns.indexOf(loop.startLabel) <= targetIndex && target.insns.indexOf(loop.endLabel!!) >= targetIndex
        }
        val loop = if (depth == -1) loopStack.last() else loopStack[depth]
        jumpTarget = loop.endLabel!!
        loadFrames(analyze(target), target, node)
    }
}