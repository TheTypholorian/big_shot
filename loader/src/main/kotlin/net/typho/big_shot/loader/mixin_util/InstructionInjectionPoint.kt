package net.typho.big_shot.loader.mixin_util

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData

@InjectionPoint.AtCode(namespace = "BIG_SHOT", value = "INSN")
class InstructionInjectionPoint(
    data: InjectionPointData
) : InjectionPoint(data) {
    @JvmField
    val opcode = data.opcode
    @JvmField
    val ordinal = data.ordinal

    override fun find(
        desc: String,
        insns: InsnList,
        nodes: MutableCollection<AbstractInsnNode>
    ): Boolean {
        var found = false
        var ordinal = 0

        for (insn in insns) {
            if (opcode == -1 || insn.opcode == opcode) {
                if (this.ordinal == -1 || this.ordinal == ordinal) {
                    nodes.add(insn)
                    found = true
                }

                ordinal++
            }
        }

        return found
    }
}