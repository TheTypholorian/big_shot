package net.typho.big_shot.api.client.rendering.shaders.reflect

import net.typho.big_shot.api.client.rendering.shaders.bytecode.IShaderInsn
import net.typho.big_shot.api.client.rendering.shaders.bytecode.OP_STORE
import net.typho.big_shot.api.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.api.client.rendering.shaders.bytecode.ShaderInsnNode
import net.typho.big_shot.api.client.rendering.shaders.bytecode.ShaderLabelNode
import net.typho.big_shot.api.client.rendering.shaders.bytecode.ShaderVariable

sealed interface ShaderLocal {
    val type: ShaderBytecodeType?
        get() = null

    fun load(insns: MutableList<IShaderInsn>, branch: ShaderMethodBranch): ShaderStackValue? = null

    fun store(insns: MutableList<IShaderInsn>, branch: ShaderMethodBranch, value: ShaderStackValue): Unit? = null

    data class Variable(
        @JvmField
        val variable: ShaderVariable
    ) : ShaderLocal {
        override val type: ShaderBytecodeType
            get() = variable.type.type

        override fun load(insns: MutableList<IShaderInsn>, branch: ShaderMethodBranch) = ShaderStackValue.LoadVariable(variable)

        override fun store(insns: MutableList<IShaderInsn>, branch: ShaderMethodBranch, value: ShaderStackValue) {
            insns.add(ShaderInsnNode(OP_STORE, variable.label, value.load(branch)))
        }
    }

    data class Argument(
        @JvmField
        val label: ShaderLabelNode,
        override val type: ShaderBytecodeType
    ) : ShaderLocal {
        override fun load(insns: MutableList<IShaderInsn>, branch: ShaderMethodBranch) = ShaderStackValue.Label(label, type)

        override fun store(insns: MutableList<IShaderInsn>, branch: ShaderMethodBranch, value: ShaderStackValue) {
            throw JavaShaderCompilationException("Cannot modify an argument's value, create a new variable.")
        }
    }

    data class NewArray(
        override val type: ShaderBytecodeType.Array,
        @JvmField
        val name: String?
    ) : ShaderLocal

    object This : ShaderLocal
}