package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.IShaderInsn
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.OP_LOAD
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeBuilder
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderConstant
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderInsnNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderLabelNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderVariable

sealed interface ShaderStackValue {
    val label: ShaderLabelNode?
        get() = null
    val type: ShaderBytecodeType?
        get() = null

    fun tryMerge(other: ShaderStackValue): (() -> ShaderStackValue)? {
        return if (this == other) { { this } } else null
    }

    data class Label(
        override val label: ShaderLabelNode,
        override val type: ShaderBytecodeType?
    ) : ShaderStackValue

    class LoadVariable(
        insns: MutableList<IShaderInsn>,
        @JvmField
        val variable: ShaderVariable
    ) : ShaderStackValue {
        override val label: ShaderLabelNode by lazy {
            ShaderLabelNode().also { insns.add(ShaderInsnNode(OP_LOAD, variable.type.type, it, variable.label)) }
        }
        override val type: ShaderBytecodeType
            get() = variable.type.type

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is LoadVariable) return false

            if (variable != other.variable) return false

            return true
        }

        override fun hashCode(): Int {
            return variable.hashCode()
        }

        override fun toString(): String {
            return "LoadVariable(variable=$variable)"
        }
    }

    data class Constant(
        @JvmField
        val builder: ShaderBytecodeBuilder,
        @JvmField
        val const: ShaderConstant
    ) : ShaderStackValue {
        override val label: ShaderLabelNode by lazy { builder.getConstant(const) }
        override val type: ShaderBytecodeType
            get() = const.type

        /*
        override fun tryMerge(other: StackValue): (() -> StackValue)? {
            return when (other) {
                this -> {
                    { this }
                }
                is Constant if type == other.type -> {
                    {
                        Label(null, type)
                    }
                }
                else -> null
            }
        }
         */
    }

    data class StringConstant(
        @JvmField
        val const: String
    ) : ShaderStackValue

    data class Comparison(
        @JvmField
        val javaOpcode: Int,
        @JvmField
        val right: ShaderStackValue,
        @JvmField
        val left: ShaderStackValue
    ) : ShaderStackValue

    data class Array(
        @JvmField
        val variable: ShaderVariable
    ) : ShaderStackValue {
        override val type: ShaderBytecodeType
            get() = variable.type.type
    }

    data class NewObject(
        @JvmField
        val index: Int
    ) : ShaderStackValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NewObject) return false

            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            return index
        }
    }

    object This : ShaderStackValue
}