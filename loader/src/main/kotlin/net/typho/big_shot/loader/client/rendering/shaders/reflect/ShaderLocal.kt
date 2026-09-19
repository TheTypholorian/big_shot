package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.IShaderInsn
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.OP_STORE
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderInsnNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderLabelNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderVariable

sealed interface ShaderLocal {
    val type: ShaderBytecodeType?
        get() = null

    fun load(compiler: ShaderMethodCompiler): ShaderStackValue? = null

    fun store(compiler: ShaderMethodCompiler, value: ShaderStackValue): Unit? = null

    data class Variable(
        @JvmField
        val insns: MutableList<IShaderInsn>,
        @JvmField
        val variable: ShaderVariable
    ) : ShaderLocal {
        override val type: ShaderBytecodeType
            get() = variable.type.type

        override fun load(compiler: ShaderMethodCompiler) = ShaderStackValue.LoadVariable(insns, variable)

        override fun store(compiler: ShaderMethodCompiler, value: ShaderStackValue) {
            insns.add(ShaderInsnNode(OP_STORE, variable.label, value.label!!))
        }
    }

    data class Argument(
        @JvmField
        val label: ShaderLabelNode,
        override val type: ShaderBytecodeType
    ) : ShaderLocal {
        override fun load(compiler: ShaderMethodCompiler) = ShaderStackValue.Label(label, type)

        override fun store(compiler: ShaderMethodCompiler, value: ShaderStackValue) {
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