package net.typho.big_shot.loader.mixin.target

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.TypeInsnNode
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData

@InjectionPoint.AtCode(namespace = "BIG_SHOT", value = "TYPE")
class TypeInjectionPoint(
    data: InjectionPointData
) : InjectionPoint(data) {
    @JvmField
    val desc = data.get("target", "")
    @JvmField
    val opcode = data.getOpcode(-1, Opcodes.NEW, Opcodes.ANEWARRAY, Opcodes.CHECKCAST, Opcodes.INSTANCEOF)
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
            if (insn is TypeInsnNode && (opcode == -1 || insn.opcode == opcode) && (this.desc.isEmpty() || insn.desc == this.desc)) {
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