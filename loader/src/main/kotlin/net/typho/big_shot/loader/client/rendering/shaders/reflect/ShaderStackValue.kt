package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.*

sealed interface ShaderStackValue {
    val type: ShaderBytecodeType?
        get() = null

    fun load(branch: ShaderMethodBranch): ShaderLabelNode? = null

    data class Label(
        @JvmField
        val label: ShaderLabelNode,
        override val type: ShaderBytecodeType?
    ) : ShaderStackValue {
        override fun load(branch: ShaderMethodBranch) = label
    }

    class LoadVariable(
        @JvmField
        val variable: ShaderVariable
    ) : ShaderStackValue {
        override val type: ShaderBytecodeType
            get() = variable.type.type

        override fun load(branch: ShaderMethodBranch): ShaderLabelNode {
            val label = ShaderLabelNode()
            branch.insns.add(ShaderInsnNode(OP_LOAD, variable.type.type, label, variable.label))
            return label
        }

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
        val const: ShaderConstant
    ) : ShaderStackValue {
        override val type: ShaderBytecodeType
            get() = const.type

        override fun load(branch: ShaderMethodBranch): ShaderLabelNode {
            return branch.method.cls.builder.getConstant(const)
        }

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