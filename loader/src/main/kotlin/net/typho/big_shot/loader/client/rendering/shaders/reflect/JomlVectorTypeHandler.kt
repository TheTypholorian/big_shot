package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode

abstract class JomlVectorTypeHandler(
    @JvmField
    val mutableClassType: Type,
    @JvmField
    val immutableClassType: Type,
    @JvmField
    val componentType: Type,
    @JvmField
    val mutable: Boolean,
    @JvmField
    val type: ShaderBytecodeType.Vector
) : ShaderTypeHandler {
    object Double2 : DoubleVector(Type.getType("Lorg/joml/Vector2d;"), Type.getType("Lorg/joml/Vector2dc;"), true, ShaderBytecodeType.VECTOR2D)
    object Double3 : DoubleVector(Type.getType("Lorg/joml/Vector3d;"), Type.getType("Lorg/joml/Vector3dc;"), true, ShaderBytecodeType.VECTOR3D)
    object Double4 : DoubleVector(Type.getType("Lorg/joml/Vector4d;"), Type.getType("Lorg/joml/Vector4dc;"), true, ShaderBytecodeType.VECTOR4D)

    object Float2 : FloatVector(Type.getType("Lorg/joml/Vector2f;"), Type.getType("Lorg/joml/Vector2fc;"), true, ShaderBytecodeType.VECTOR2F)
    object Float3 : FloatVector(Type.getType("Lorg/joml/Vector3f;"), Type.getType("Lorg/joml/Vector3fc;"), true, ShaderBytecodeType.VECTOR3F)
    object Float4 : FloatVector(Type.getType("Lorg/joml/Vector4f;"), Type.getType("Lorg/joml/Vector4fc;"), true, ShaderBytecodeType.VECTOR4F)

    object Long2 : LongVector(Type.getType("Lorg/joml/Vector2L;"), Type.getType("Lorg/joml/Vector2Lc;"), true, ShaderBytecodeType.VECTOR2L)
    object Long3 : LongVector(Type.getType("Lorg/joml/Vector3L;"), Type.getType("Lorg/joml/Vector3Lc;"), true, ShaderBytecodeType.VECTOR3L)
    object Long4 : LongVector(Type.getType("Lorg/joml/Vector4L;"), Type.getType("Lorg/joml/Vector4Lc;"), true, ShaderBytecodeType.VECTOR4L)

    object Int2 : IntVector(Type.getType("Lorg/joml/Vector2i;"), Type.getType("Lorg/joml/Vector2ic;"), true, ShaderBytecodeType.VECTOR2I)
    object Int3 : IntVector(Type.getType("Lorg/joml/Vector3i;"), Type.getType("Lorg/joml/Vector3ic;"), true, ShaderBytecodeType.VECTOR3I)
    object Int4 : IntVector(Type.getType("Lorg/joml/Vector4i;"), Type.getType("Lorg/joml/Vector4ic;"), true, ShaderBytecodeType.VECTOR4I)

    object Double2c : DoubleVector(Type.getType("Lorg/joml/Vector2d;"), Type.getType("Lorg/joml/Vector2dc;"), false, ShaderBytecodeType.VECTOR2D)
    object Double3c : DoubleVector(Type.getType("Lorg/joml/Vector3d;"), Type.getType("Lorg/joml/Vector3dc;"), false, ShaderBytecodeType.VECTOR3D)
    object Double4c : DoubleVector(Type.getType("Lorg/joml/Vector4d;"), Type.getType("Lorg/joml/Vector4dc;"), false, ShaderBytecodeType.VECTOR4D)

    object Float2c : FloatVector(Type.getType("Lorg/joml/Vector2f;"), Type.getType("Lorg/joml/Vector2fc;"), false, ShaderBytecodeType.VECTOR2F)
    object Float3c : FloatVector(Type.getType("Lorg/joml/Vector3f;"), Type.getType("Lorg/joml/Vector3fc;"), false, ShaderBytecodeType.VECTOR3F)
    object Float4c : FloatVector(Type.getType("Lorg/joml/Vector4f;"), Type.getType("Lorg/joml/Vector4fc;"), false, ShaderBytecodeType.VECTOR4F)

    object Long2c : LongVector(Type.getType("Lorg/joml/Vector2L;"), Type.getType("Lorg/joml/Vector2Lc;"), false, ShaderBytecodeType.VECTOR2L)
    object Long3c : LongVector(Type.getType("Lorg/joml/Vector3L;"), Type.getType("Lorg/joml/Vector3Lc;"), false, ShaderBytecodeType.VECTOR3L)
    object Long4c : LongVector(Type.getType("Lorg/joml/Vector4L;"), Type.getType("Lorg/joml/Vector4Lc;"), false, ShaderBytecodeType.VECTOR4L)

    object Int2c : IntVector(Type.getType("Lorg/joml/Vector2i;"), Type.getType("Lorg/joml/Vector2ic;"), false, ShaderBytecodeType.VECTOR2I)
    object Int3c : IntVector(Type.getType("Lorg/joml/Vector3i;"), Type.getType("Lorg/joml/Vector3ic;"), false, ShaderBytecodeType.VECTOR3I)
    object Int4c : IntVector(Type.getType("Lorg/joml/Vector4i;"), Type.getType("Lorg/joml/Vector4ic;"), false, ShaderBytecodeType.VECTOR4I)

    companion object : ShaderTypeHandler.Supplier {
        @JvmStatic
        fun getDouble(count: Int) = when (count) {
            2 -> Double2
            3 -> Double3
            4 -> Double4
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getFloat(count: Int) = when (count) {
            2 -> Float2
            3 -> Float3
            4 -> Float4
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getLong(count: Int) = when (count) {
            2 -> Long2
            3 -> Long3
            4 -> Long4
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getInt(count: Int) = when (count) {
            2 -> Int2
            3 -> Int3
            4 -> Int4
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getDoublec(count: Int) = when (count) {
            2 -> Double2c
            3 -> Double3c
            4 -> Double4c
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getFloatc(count: Int) = when (count) {
            2 -> Float2c
            3 -> Float3c
            4 -> Float4c
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getLongc(count: Int) = when (count) {
            2 -> Long2c
            3 -> Long3c
            4 -> Long4c
            else -> throw IllegalArgumentException(count.toString())
        }

        @JvmStatic
        fun getIntc(count: Int) = when (count) {
            2 -> Int2c
            3 -> Int3c
            4 -> Int4c
            else -> throw IllegalArgumentException(count.toString())
        }

        override fun getTypeHandler(type: Type): ShaderTypeHandler? {
            return when (type.internalName) {
                "org/joml/Vector2d" -> Double2
                "org/joml/Vector3d" -> Double3
                "org/joml/Vector4d" -> Double4
                "org/joml/Vector2f" -> Float2
                "org/joml/Vector3f" -> Float3
                "org/joml/Vector4f" -> Float4
                "org/joml/Vector2L" -> Long2
                "org/joml/Vector3L" -> Long3
                "org/joml/Vector4L" -> Long4
                "org/joml/Vector2i" -> Int2
                "org/joml/Vector3i" -> Int3
                "org/joml/Vector4i" -> Int4
                "org/joml/Vector2dc" -> Double2c
                "org/joml/Vector3dc" -> Double3c
                "org/joml/Vector4dc" -> Double4c
                "org/joml/Vector2fc" -> Float2c
                "org/joml/Vector3fc" -> Float3c
                "org/joml/Vector4fc" -> Float4c
                "org/joml/Vector2Lc" -> Long2c
                "org/joml/Vector3Lc" -> Long3c
                "org/joml/Vector4Lc" -> Long4c
                "org/joml/Vector2ic" -> Int2c
                "org/joml/Vector3ic" -> Int3c
                "org/joml/Vector4ic" -> Int4c
                else -> null
            }
        }

        fun ShaderMethodBranch.createVector(type: ShaderBytecodeType.Vector, vararg values: ShaderStackValue): ShaderLabelNode {
            return if (values.all { it is ShaderStackValue.Constant }) {
                method.cls.builder.getConstant(ShaderConstant(type, values.map { it.label!! }))
            } else {
                val result = ShaderLabelNode()
                insns.add(ShaderInsnNode(OP_COMPOSITE_CONSTRUCT, type, result, values.map { it.label }))
                result
            }
        }

        fun ShaderMethodBranch.vectorStore(result: ShaderLabelNode, dest: ShaderStackValue) {
            if (dest is ShaderStackValue.LoadVariable) {
                insns.add(ShaderInsnNode(OP_STORE, dest.variable.label, result))
            }
        }

        fun ShaderMethodBranch.vectorStoreLoad(result: ShaderLabelNode, dest: ShaderStackValue) {
            if (dest is ShaderStackValue.LoadVariable) {
                insns.add(ShaderInsnNode(OP_STORE, dest.variable.label, result))
                frame.push(ShaderStackValue.LoadVariable(insns, dest.variable))
            } else {
                frame.push(ShaderStackValue.Label(result, dest.type))
            }
        }

        fun ShaderMethodBranch.vectorOp(opcode: Int, type: ShaderBytecodeType.Vector, dest: ShaderStackValue, add: ShaderLabelNode, self: ShaderLabelNode) {
            val result = ShaderLabelNode()
            insns.add(ShaderInsnNode(opcode, type, result, self, add))
            vectorStoreLoad(result, dest)
        }

        fun ShaderMethodBranch.vectorOpSelf(opcode: Int, type: ShaderBytecodeType.Vector, add: ShaderLabelNode, self: ShaderStackValue) {
            vectorOp(opcode, type, self, add, self.label!!)
        }

        fun ShaderMethodBranch.vectorInit(type: ShaderBytecodeType.Vector, vararg values: ShaderStackValue) {
            val vec = createVector(type, *values)
            val self = frame.pop() as ShaderStackValue.NewObject
            frame.replace(self, ShaderStackValue.Label(vec, type))
        }
    }

    val classType: Type
        get() = if (mutable) mutableClassType else immutableClassType

    @JvmField
    val voidSinglePrimDesc = Type.getMethodDescriptor(Type.VOID_TYPE, componentType)
    @JvmField
    val voidPrimDesc = Type.getMethodDescriptor(Type.VOID_TYPE, *Array(type.componentCount) { componentType })
    @JvmField
    val voidImmutableDesc = Type.getMethodDescriptor(Type.VOID_TYPE, immutableClassType)
    @JvmField
    val voidPrimArrayDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType("[$componentType"))

    @JvmField
    val opSinglePrimDestDesc = Type.getMethodDescriptor(mutableClassType, componentType, mutableClassType)
    @JvmField
    val opPrimDestDesc = Type.getMethodDescriptor(mutableClassType, *Array(type.componentCount) { componentType }, mutableClassType)
    @JvmField
    val opImmutableDestDesc = Type.getMethodDescriptor(mutableClassType, immutableClassType, mutableClassType)
    @JvmField
    val opSinglePrimSelfDesc = Type.getMethodDescriptor(mutableClassType, componentType)
    @JvmField
    val opPrimSelfDesc = Type.getMethodDescriptor(mutableClassType, *Array(type.componentCount) { componentType })
    @JvmField
    val opImmutableSelfDesc = Type.getMethodDescriptor(mutableClassType, immutableClassType)

    override fun handleFieldOp(branch: ShaderMethodBranch, op: FieldInsnNode) {
        val success = when (op.name) {
            "x" -> handleComponentOp(branch, op, 0)
            "y" -> handleComponentOp(branch, op, 1)
            "z" -> handleComponentOp(branch, op, 2)
            "w" -> handleComponentOp(branch, op, 3)
            else -> false
        }

        if (!success) {
            throw JavaShaderCompilationException("Unsupported field ${op.owner}.${op.name} ${op.desc}")
        }
    }

    override fun handleMethodCall(
        branch: ShaderMethodBranch,
        call: MethodInsnNode
    ) {
        val success = when (call.name) {
            "<init>" -> handleConstructor(branch, call.desc)
            "add" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_ADD else OP_F_ADD, call.desc)
            "sub" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_SUB else OP_F_SUB, call.desc)
            "mul" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_MUL else OP_F_MUL, call.desc)
            "div" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_S_DIV else OP_F_DIV, call.desc)
            else -> false
        }

        if (!success) {
            throw JavaShaderCompilationException("Unsupported method ${call.owner}.${call.name}${call.desc}")
        }
    }

    override fun handleCastFrom(branch: ShaderMethodBranch, from: ShaderStackValue) {
        // TODO error on illegal cast
    }

    protected open fun handleComponentOp(branch: ShaderMethodBranch, op: FieldInsnNode, index: Int): Boolean {
        if (index < 0 || index >= type.componentCount) {
            return false
        }

        when (op.opcode) {
            Opcodes.GETFIELD -> {
                val vector = branch.frame.pop().label!!

                val result = ShaderLabelNode()
                branch.insns.add(ShaderInsnNode(OP_COMPOSITE_EXTRACT, type, result, vector, index))
                branch.frame.push(ShaderStackValue.Label(result, type.componentType))
                return true
            }
            Opcodes.PUTFIELD -> {
                val value = branch.frame.pop().label!!
                val vector = branch.frame.pop().label!!

                val ptr = ShaderLabelNode()
                branch.insns.add(ShaderInsnNode(OP_ACCESS_CHAIN, ShaderBytecodeType.Pointer(STORAGE_CLASS_FUNCTION, type.componentType), ptr, vector, branch.method.cls.builder.getConstant(ShaderConstant(ShaderBytecodeType.INT, listOf(index)))))
                branch.insns.add(ShaderInsnNode(OP_STORE, ptr, value))
                return true
            }
        }

        return false
    }

    protected open fun handleSimpleOp(branch: ShaderMethodBranch, opcode: Int, desc: String): Boolean {
        when (desc) {
            opSinglePrimDestDesc -> branch.vectorOp(opcode, type, branch.frame.pop(), branch.createVector(type, branch.frame.pop()), branch.frame.pop().label!!)
            opPrimDestDesc -> branch.vectorOp(opcode, type, branch.frame.pop(), branch.createVector(type, *branch.frame.popVectorComponents(type)), branch.frame.pop().label!!)
            opImmutableDestDesc -> branch.vectorOp(opcode, type, branch.frame.pop(), branch.frame.pop().label!!, branch.frame.pop().label!!)

            opSinglePrimSelfDesc -> branch.vectorOpSelf(opcode, type, branch.createVector(type, branch.frame.pop()), branch.frame.pop())
            opPrimSelfDesc -> branch.vectorOpSelf(opcode, type, branch.createVector(type, *branch.frame.popVectorComponents(type)), branch.frame.pop())
            opImmutableSelfDesc -> branch.vectorOpSelf(opcode, type, branch.frame.pop().label!!, branch.frame.pop())

            else -> return false
        }

        return true
    }

    protected open fun handleNoArgVector(branch: ShaderMethodBranch) {
        val zero = ShaderStackValue.Label(branch.method.cls.builder.getConstant((type.componentType as ShaderBytecodeType.Numerical).getConstant(0)), type.componentType)

        if (type.componentCount == 4) {
            val one = ShaderStackValue.Label(branch.method.cls.builder.getConstant(type.componentType.getConstant(1)), type.componentType)
            branch.vectorInit(type, zero, zero, zero, one)
        } else {
            branch.vectorInit(type, zero)
        }
    }

    protected abstract fun castConstructorType(branch: ShaderMethodBranch, arg: Type, input: ShaderStackValue, type: ShaderBytecodeType.Vector): ShaderStackValue?

    protected open fun handleConstructor(branch: ShaderMethodBranch, desc: String): Boolean {
        when (desc) {
            "()V" -> handleNoArgVector(branch)
            voidSinglePrimDesc -> branch.vectorInit(type, branch.frame.pop())
            voidPrimDesc -> branch.vectorInit(type, *branch.frame.popVectorComponents(type))
            voidImmutableDesc -> branch.vectorInit(type, branch.frame.pop())
            voidPrimArrayDesc -> {
                val array = branch.frame.pop() as ShaderStackValue.LoadVariable
                val components = Array(type.componentCount) { index ->
                    val pointer = ShaderLabelNode()
                    val value = ShaderLabelNode()
                    branch.insns.add(ShaderInsnNode(OP_ACCESS_CHAIN, array.variable.type, pointer, array.variable.label, index))
                    branch.insns.add(ShaderInsnNode(OP_LOAD, type.componentType, value, pointer))
                    ShaderStackValue.Label(value, type.componentType)
                }
                branch.vectorInit(type, *components)
            }
            else -> {
                val args = Type.getArgumentTypes(desc)

                when (args.size) {
                    1 -> {
                        if (type.componentCount == 2) {
                            when (args[0]) {
                                Double3c.classType, Float3c.classType, Int3c.classType -> { // TODO abstractify
                                    val targetLabel = ShaderLabelNode()
                                    branch.insns.add(ShaderInsnNode(OP_VECTOR_SHUFFLE, type, targetLabel, branch.frame.pop().label!!, 0, 1))
                                    branch.vectorInit(type, ShaderStackValue.Label(targetLabel, type))
                                    return true
                                }
                            }
                        }

                        val input = branch.frame.pop()
                        val processed = castConstructorType(branch, args[0], input, type) ?: return false
                        branch.vectorInit(type, processed)
                        return true
                    }
                    2 -> {
                        if (args[1] == componentType && type.componentCount > 2) {
                            val z = branch.frame.pop()
                            val xy = branch.frame.pop()

                            val processed = castConstructorType(branch, args[0], xy, type.copy(componentCount = type.componentCount - 1)) ?: return false
                            branch.vectorInit(type, processed, z)
                            return true
                        }
                    }
                    3 -> {
                        if (args[1] == componentType && args[2] == componentType && type.componentCount > 3) {
                            val w = branch.frame.pop()
                            val z = branch.frame.pop()
                            val xy = branch.frame.pop()

                            val processed = castConstructorType(branch, args[0], xy, type.copy(componentCount = type.componentCount - 2)) ?: return false
                            branch.vectorInit(type, processed, z, w)
                            return true
                        }
                    }
                }

                return false
            }
        }

        return true
    }

    open class DoubleVector(
        mutableClassType: Type,
        immutableClassType: Type,
        mutable: Boolean,
        type: ShaderBytecodeType.Vector
    ) : JomlVectorTypeHandler(mutableClassType, immutableClassType, Type.DOUBLE_TYPE, mutable, type) {
        override fun castConstructorType(
            branch: ShaderMethodBranch,
            arg: Type,
            input: ShaderStackValue,
            type: ShaderBytecodeType.Vector
        ): ShaderStackValue? {
            return when (arg) {
                getDoublec(type.componentCount).classType -> input
                getIntc(type.componentCount).classType -> {
                    val targetLabel = ShaderLabelNode()
                    branch.insns.add(ShaderInsnNode(OP_CONVERT_S_TO_F, type, targetLabel, input.label))
                    ShaderStackValue.Label(targetLabel, type)
                }
                else -> null
            }
        }
    }

    open class FloatVector(
        mutableClassType: Type,
        immutableClassType: Type,
        mutable: Boolean,
        type: ShaderBytecodeType.Vector
    ) : JomlVectorTypeHandler(mutableClassType, immutableClassType, Type.FLOAT_TYPE, mutable, type) {
        override fun castConstructorType(
            branch: ShaderMethodBranch,
            arg: Type,
            input: ShaderStackValue,
            type: ShaderBytecodeType.Vector
        ): ShaderStackValue? {
            return when (arg) {
                getFloatc(type.componentCount).classType -> input
                getDoublec(type.componentCount).classType -> {
                    val targetLabel = ShaderLabelNode()
                    branch.insns.add(ShaderInsnNode(OP_F_CONVERT, type, targetLabel, input.label))
                    ShaderStackValue.Label(targetLabel, type)
                }
                getIntc(type.componentCount).classType -> {
                    val targetLabel = ShaderLabelNode()
                    branch.insns.add(ShaderInsnNode(OP_CONVERT_S_TO_F, type, targetLabel, input.label))
                    ShaderStackValue.Label(targetLabel, type)
                }
                else -> null
            }
        }
    }

    open class LongVector(
        mutableClassType: Type,
        immutableClassType: Type,
        mutable: Boolean,
        type: ShaderBytecodeType.Vector
    ) : JomlVectorTypeHandler(mutableClassType, immutableClassType, Type.LONG_TYPE, mutable, type) {
        override fun castConstructorType(
            branch: ShaderMethodBranch,
            arg: Type,
            input: ShaderStackValue,
            type: ShaderBytecodeType.Vector
        ): ShaderStackValue? {
            return when (arg) {
                getLongc(type.componentCount).classType -> input
                getIntc(type.componentCount).classType -> {
                    val targetLabel = ShaderLabelNode()
                    branch.insns.add(ShaderInsnNode(OP_S_CONVERT, type, targetLabel, input.label))
                    ShaderStackValue.Label(targetLabel, type)
                }
                else -> null
            }
        }
    }

    open class IntVector(
        mutableClassType: Type,
        immutableClassType: Type,
        mutable: Boolean,
        type: ShaderBytecodeType.Vector
    ) : JomlVectorTypeHandler(mutableClassType, immutableClassType, Type.INT_TYPE, mutable, type) {
        override fun castConstructorType(
            branch: ShaderMethodBranch,
            arg: Type,
            input: ShaderStackValue,
            type: ShaderBytecodeType.Vector
        ): ShaderStackValue? {
            return when (arg) {
                getIntc(type.componentCount).classType -> input
                else -> null
            }
        }
    }
}