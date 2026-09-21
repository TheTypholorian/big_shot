package net.typho.big_shot.api.client.rendering.shaders.reflect

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode

object KotlinIntrinsicsTypeHandler : ShaderTypeHandler, ShaderTypeHandler.Supplier {
    override fun getTypeHandler(type: Type): ShaderTypeHandler? {
        return if (type.internalName == "kotlin/jvm/internal/Intrinsics") this else null
    }

    override fun handleFieldOp(
        branch: ShaderMethodBranch,
        op: FieldInsnNode
    ) {
    }

    override fun handleMethodCall(
        branch: ShaderMethodBranch,
        call: MethodInsnNode
    ) {
        repeat(Type.getArgumentCount(call.desc)) {
            branch.frame.pop()
        }

        if (call.opcode != Opcodes.INVOKESTATIC) {
            branch.frame.pop()
        }
    }

    override fun handleCastFrom(branch: ShaderMethodBranch, from: ShaderStackValue) {
    }
}