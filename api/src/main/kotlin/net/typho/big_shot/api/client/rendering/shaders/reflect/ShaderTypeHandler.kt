package net.typho.big_shot.api.client.rendering.shaders.reflect

import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode

interface ShaderTypeHandler {
    fun handleFieldOp(branch: ShaderMethodBranch, op: FieldInsnNode)

    fun handleMethodCall(branch: ShaderMethodBranch, call: MethodInsnNode)

    fun handleCastFrom(branch: ShaderMethodBranch, from: ShaderStackValue)

    interface Supplier {
        fun getTypeHandler(type: Type): ShaderTypeHandler?
    }
}