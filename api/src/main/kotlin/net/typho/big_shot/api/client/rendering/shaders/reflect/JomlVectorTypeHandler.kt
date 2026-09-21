package net.typho.big_shot.api.client.rendering.shaders.reflect

import net.typho.big_shot.api.client.rendering.shaders.bytecode.*
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
            if (values.size != type.componentCount) {
                throw IllegalArgumentException()
            }

            return if (values.all { it is ShaderStackValue.Constant }) {
                method.cls.builder.getConstant(ShaderConstant(type, values.map { it.load(this) }))
            } else {
                val result = ShaderLabelNode()
                insns.add(ShaderInsnNode(OP_COMPOSITE_CONSTRUCT, type, result, values.map { it.load(this) }))
                result
            }
        }

        fun ShaderMethodBranch.createVector(type: ShaderBytecodeType.Vector, value: ShaderStackValue): ShaderLabelNode {
            return createVector(type, *Array(type.componentCount) { value })
        }

        fun ShaderMethodBranch.vectorStore(result: ShaderLabelNode, dest: ShaderStackValue) {
            if (dest is ShaderStackValue.LoadVariable) {
                insns.add(ShaderInsnNode(OP_STORE, dest.variable.label, result))
            }
        }

        fun ShaderMethodBranch.vectorStoreLoad(result: ShaderLabelNode, dest: ShaderStackValue) {
            if (dest is ShaderStackValue.LoadVariable) {
                insns.add(ShaderInsnNode(OP_STORE, dest.variable.label, result))
                frame.push(ShaderStackValue.LoadVariable(dest.variable))
            } else {
                frame.push(ShaderStackValue.Label(result, dest.type))
            }
        }

        fun ShaderMethodBranch.vectorOp(opcode: Int, type: ShaderBytecodeType.Vector, dest: ShaderStackValue, other: ShaderLabelNode, self: ShaderLabelNode) {
            val result = ShaderLabelNode()
            insns.add(ShaderInsnNode(opcode, type, result, self, other))
            vectorStoreLoad(result, dest)
        }

        fun ShaderMethodBranch.vectorOpSelf(opcode: Int, type: ShaderBytecodeType.Vector, other: ShaderLabelNode, self: ShaderStackValue) {
            vectorOp(opcode, type, self, other, self.load(this))
        }

        fun ShaderMethodBranch.vectorOpExt(set: ShaderLabelNode, opcode: Int, type: ShaderBytecodeType.Vector, dest: ShaderStackValue, other: ShaderLabelNode, self: ShaderLabelNode) {
            val result = ShaderLabelNode()
            insns.add(ShaderInsnNode(OP_EXT_INST, type, result, set, opcode, self, other))
            vectorStoreLoad(result, dest)
        }

        fun ShaderMethodBranch.vectorOpExtSelf(set: ShaderLabelNode, opcode: Int, type: ShaderBytecodeType.Vector, other: ShaderLabelNode, self: ShaderStackValue) {
            vectorOpExt(set, opcode, type, self, other, self.load(this))
        }

        fun ShaderMethodBranch.vectorDoubleOp(opcode1: Int, opcode2: Int, type: ShaderBytecodeType.Vector, dest: ShaderStackValue, b: ShaderLabelNode, a: ShaderLabelNode, self: ShaderLabelNode) {
            val resultA = ShaderLabelNode()
            val resultB = ShaderLabelNode()
            insns.add(ShaderInsnNode(opcode1, type, resultA, self, a))
            insns.add(ShaderInsnNode(opcode2, type, resultB, resultA, b))
            vectorStoreLoad(resultB, dest)
        }

        fun ShaderMethodBranch.vectorDoubleOpSelf(opcode1: Int, opcode2: Int, type: ShaderBytecodeType.Vector, b: ShaderLabelNode, a: ShaderLabelNode, self: ShaderStackValue) {
            vectorDoubleOp(opcode1, opcode2, type, self, b, a, self.load(this))
        }

        fun ShaderMethodBranch.vectorInit(type: ShaderBytecodeType.Vector, vararg values: ShaderStackValue) {
            val vec = createVector(type, *values)
            val self = frame.pop() as ShaderStackValue.NewObject
            frame.replace(self, ShaderStackValue.Label(vec, type))
        }

        fun ShaderMethodBranch.vectorInit(type: ShaderBytecodeType.Vector, value: ShaderStackValue) {
            vectorInit(type, *Array(type.componentCount) { value })
        }
    }

    val classType: Type
        get() = if (mutable) mutableClassType else immutableClassType

    @JvmField
    val voidSinglePrimDesc = Type.getMethodDescriptor(Type.VOID_TYPE, componentType) // (F)V
    @JvmField
    val voidPrimDesc = Type.getMethodDescriptor(Type.VOID_TYPE, *Array(type.componentCount) { componentType }) // (FFF)V
    @JvmField
    val voidImmutableDesc = Type.getMethodDescriptor(Type.VOID_TYPE, immutableClassType) // (Lorg/joml/Vector3fc;)V
    @JvmField
    val voidPrimArrayDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType("[$componentType")) // ([F)V

    @JvmField
    val opSinglePrimDestDesc = Type.getMethodDescriptor(mutableClassType, componentType, mutableClassType) // (FLorg/joml/Vector3f;)Lorg/joml/Vector3f;
    @JvmField
    val opPrimDestDesc = Type.getMethodDescriptor(mutableClassType, *Array(type.componentCount) { componentType }, mutableClassType) // (FFFLorg/joml/Vector3f;)Lorg/joml/Vector3f;
    @JvmField
    val opImmutableDestDesc = Type.getMethodDescriptor(mutableClassType, immutableClassType, mutableClassType) // (Lorg/joml/Vector3fc;Lorg/joml/Vector3f;)Lorg/joml/Vector3f;
    @JvmField
    val opSinglePrimSelfDesc = Type.getMethodDescriptor(mutableClassType, componentType) // (F)Lorg/joml/Vector3f;
    @JvmField
    val opPrimSelfDesc = Type.getMethodDescriptor(mutableClassType, *Array(type.componentCount) { componentType }) // (FFF)Lorg/joml/Vector3f;
    @JvmField
    val opImmutableSelfDesc = Type.getMethodDescriptor(mutableClassType, immutableClassType) // (Lorg/joml/Vector3fc;)Lorg/joml/Vector3f;

    @JvmField
    val opSinglePrimImmutableDestDesc = Type.getMethodDescriptor(mutableClassType, componentType, immutableClassType, mutableClassType) // (FLorg/joml/Vector3fc;Lorg/joml/Vector3f;)Lorg/joml/Vector3f;
    @JvmField
    val opImmutableImmutableDestDesc = Type.getMethodDescriptor(mutableClassType, immutableClassType, immutableClassType, mutableClassType) // (Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lorg/joml/Vector3f;)Lorg/joml/Vector3f;
    @JvmField
    val opSinglePrimImmutableSelfDesc = Type.getMethodDescriptor(mutableClassType, componentType, immutableClassType) // (FLorg/joml/Vector3fc;)Lorg/joml/Vector3f;
    @JvmField
    val opImmutableImmutableSelfDesc = Type.getMethodDescriptor(mutableClassType, immutableClassType, immutableClassType) // (Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)Lorg/joml/Vector3f;

    override fun handleFieldOp(branch: ShaderMethodBranch, op: FieldInsnNode) {
        val success = when (op.name) {
            "x" -> handleComponentOp(branch, op.opcode, 0)
            "y" -> handleComponentOp(branch, op.opcode, 1)
            "z" -> handleComponentOp(branch, op.opcode, 2)
            "w" -> handleComponentOp(branch, op.opcode, 3)
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
            "x" -> handleComponentOp(branch, Opcodes.GETFIELD, 0)
            "y" -> handleComponentOp(branch, Opcodes.GETFIELD, 1)
            "z" -> handleComponentOp(branch, Opcodes.GETFIELD, 2)
            "w" -> handleComponentOp(branch, Opcodes.GETFIELD, 3)
            "add" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_ADD else OP_F_ADD, call.desc)
            "sub" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_SUB else OP_F_SUB, call.desc)
            "mul" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_MUL else OP_F_MUL, call.desc)
            "div" -> handleSimpleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_S_DIV else OP_F_DIV, call.desc)
            "fma" -> handleSimpleOpExt(branch, branch.method.cls.builder.import("GLSL.std.450"), GLSL_FMA, call.desc)
            "mulAdd" -> handleDoubleOp(branch, if (type.componentType is ShaderBytecodeType.Integer) OP_I_MUL else OP_F_MUL, if (type.componentType is ShaderBytecodeType.Integer) OP_I_ADD else OP_F_ADD, call.desc)
            else -> false
        }

        if (!success) {
            throw JavaShaderCompilationException("Unsupported method ${call.owner}.${call.name}${call.desc}")
        }
    }

    override fun handleCastFrom(branch: ShaderMethodBranch, from: ShaderStackValue) {
        // TODO error on illegal cast
    }

    protected open fun handleComponentOp(branch: ShaderMethodBranch, opcode: Int, index: Int): Boolean {
        if (index < 0 || index >= type.componentCount) {
            return false
        }

        when (opcode) {
            Opcodes.GETFIELD -> {
                val vector = branch.frame.pop().load(branch)

                val result = ShaderLabelNode()
                branch.insns.add(ShaderInsnNode(OP_COMPOSITE_EXTRACT, type.componentType, result, vector, index))
                branch.frame.push(ShaderStackValue.Label(result, type.componentType))
                return true
            }
            Opcodes.PUTFIELD -> {
                val value = branch.frame.pop().load(branch)
                val vector = branch.frame.pop() as ShaderStackValue.LoadVariable

                val ptr = ShaderLabelNode()
                branch.insns.add(ShaderInsnNode(OP_ACCESS_CHAIN, ShaderBytecodeType.Pointer(STORAGE_CLASS_FUNCTION, type.componentType), ptr, vector.variable.label, branch.method.cls.builder.getConstant(ShaderConstant(ShaderBytecodeType.INT, listOf(index)))))
                branch.insns.add(ShaderInsnNode(OP_STORE, ptr, value))
                return true
            }
        }

        return false
    }

    protected open fun handleSimpleOp(branch: ShaderMethodBranch, opcode: Int, desc: String): Boolean {
        when (desc) {
            opSinglePrimDestDesc -> branch.vectorOp(opcode, type, branch.frame.pop(), branch.createVector(type, branch.frame.pop()), branch.frame.pop().load(branch))
            opPrimDestDesc -> branch.vectorOp(opcode, type, branch.frame.pop(), branch.createVector(type, *branch.frame.popVectorComponents(type)), branch.frame.pop().load(branch))
            opImmutableDestDesc -> branch.vectorOp(opcode, type, branch.frame.pop(), branch.frame.pop().load(branch), branch.frame.pop().load(branch))

            opSinglePrimSelfDesc -> branch.vectorOpSelf(opcode, type, branch.createVector(type, branch.frame.pop()), branch.frame.pop())
            opPrimSelfDesc -> branch.vectorOpSelf(opcode, type, branch.createVector(type, *branch.frame.popVectorComponents(type)), branch.frame.pop())
            opImmutableSelfDesc -> branch.vectorOpSelf(opcode, type, branch.frame.pop().load(branch), branch.frame.pop())

            else -> return false
        }

        return true
    }

    protected open fun handleSimpleOpExt(branch: ShaderMethodBranch, set: ShaderLabelNode, opcode: Int, desc: String): Boolean {
        when (desc) {
            opSinglePrimDestDesc -> branch.vectorOpExt(set, opcode, type, branch.frame.pop(), branch.createVector(type, branch.frame.pop()), branch.frame.pop().load(branch))
            opPrimDestDesc -> branch.vectorOpExt(set, opcode, type, branch.frame.pop(), branch.createVector(type, *branch.frame.popVectorComponents(type)), branch.frame.pop().load(branch))
            opImmutableDestDesc -> branch.vectorOpExt(set, opcode, type, branch.frame.pop(), branch.frame.pop().load(branch), branch.frame.pop().load(branch))

            opSinglePrimSelfDesc -> branch.vectorOpExtSelf(set, opcode, type, branch.createVector(type, branch.frame.pop()), branch.frame.pop())
            opPrimSelfDesc -> branch.vectorOpExtSelf(set, opcode, type, branch.createVector(type, *branch.frame.popVectorComponents(type)), branch.frame.pop())
            opImmutableSelfDesc -> branch.vectorOpExtSelf(set, opcode, type, branch.frame.pop().load(branch), branch.frame.pop())

            else -> return false
        }

        return true
    }

    protected open fun handleDoubleOp(branch: ShaderMethodBranch, opcode1: Int, opcode2: Int, desc: String): Boolean {
        when (desc) {
            opSinglePrimImmutableDestDesc -> branch.vectorDoubleOp(opcode1, opcode2, type, branch.frame.pop(), branch.frame.pop().load(branch), branch.createVector(type, branch.frame.pop()), branch.frame.pop().load(branch))
            opImmutableImmutableDestDesc -> branch.vectorDoubleOp(opcode1, opcode2, type, branch.frame.pop(), branch.frame.pop().load(branch), branch.frame.pop().load(branch), branch.frame.pop().load(branch))

            opSinglePrimImmutableSelfDesc -> branch.vectorDoubleOpSelf(opcode1, opcode2, type, branch.frame.pop().load(branch), branch.createVector(type, branch.frame.pop()), branch.frame.pop())
            opImmutableImmutableSelfDesc -> branch.vectorDoubleOpSelf(opcode1, opcode2, type, branch.frame.pop().load(branch), branch.frame.pop().load(branch), branch.frame.pop())

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
            voidImmutableDesc -> { // TODO might be wrong?
                val value = branch.frame.pop()
                val self = branch.frame.pop() as ShaderStackValue.NewObject
                branch.frame.replace(self, ShaderStackValue.Label(value.load(branch), value.type))
            }
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
                                    branch.insns.add(ShaderInsnNode(OP_VECTOR_SHUFFLE, type, targetLabel, branch.frame.pop().load(branch), 0, 1))
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
                    branch.insns.add(ShaderInsnNode(OP_CONVERT_S_TO_F, type, targetLabel, input.load(branch)))
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
                    branch.insns.add(ShaderInsnNode(OP_F_CONVERT, type, targetLabel, input.load(branch)))
                    ShaderStackValue.Label(targetLabel, type)
                }
                getIntc(type.componentCount).classType -> {
                    val targetLabel = ShaderLabelNode()
                    branch.insns.add(ShaderInsnNode(OP_CONVERT_S_TO_F, type, targetLabel, input.load(branch)))
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
                    branch.insns.add(ShaderInsnNode(OP_S_CONVERT, type, targetLabel, input.load(branch)))
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