package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.asm_util.cfg.BasicBlock
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.*
import net.typho.big_shot.loader.util.ExpandingByteBuffer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class ShaderMethodBranch(
    @JvmField
    val method: ShaderMethodCompiler,
    @JvmField
    val block: BasicBlock,
    parent: ShaderMethodBranch?
) : IShaderInsn {
    @JvmField
    val iterator = method.node.instructions.iterator(block.start)
    @JvmField
    val insns = mutableListOf<IShaderInsn>()
    @JvmField
    val label = ShaderLabelNode()
    @JvmField
    val frame: ShaderFrame = ShaderFrame(this, parent?.frame)
    private var ended = false

    fun hasNext() = iterator.hasNext() && iterator.nextIndex() < block.end

    override fun write(
        builder: ShaderBytecodeBuilder,
        buffer: ExpandingByteBuffer
    ) {
        insns.forEach { it.write(builder, buffer) }
    }

    fun compile() {
        insns.add(ShaderInsnNode(OP_LABEL, label))

        while (hasNext() && !ended) {
            compileNext()
        }

        if (!ended) {
            val target = method.branches[block.index + 1]!!.label
            insns.add(ShaderInsnNode(OP_BRANCH, target))
        }
    }

    fun jump(comparisonOpcode: Int, floatComparisonOpcode: Int, target: LabelNode) {
        val target = method.branches[method.cfg.blocksByInsn[target]!!]!!.label
        val value = frame.pop()
        val bool = ShaderLabelNode()

        insns.add(
            if (value is ShaderStackValue.Comparison) {
                when (value.javaOpcode) {
                    Opcodes.LCMP -> ShaderInsnNode(
                        comparisonOpcode,
                        ShaderBytecodeType.Bool,
                        bool,
                        value.left.label!!,
                        value.right.label!!
                    )
                    // TODO proper handling for NaNs
                    Opcodes.FCMPL, Opcodes.DCMPL -> ShaderInsnNode(
                        floatComparisonOpcode,
                        ShaderBytecodeType.Bool,
                        bool,
                        value.left.label!!,
                        value.right.label!!
                    )

                    Opcodes.FCMPG, Opcodes.DCMPG -> ShaderInsnNode(
                        floatComparisonOpcode,
                        ShaderBytecodeType.Bool,
                        bool,
                        value.left.label!!,
                        value.right.label!!
                    )

                    else -> throw AssertionError()
                }
            } else {
                ShaderInsnNode(
                    comparisonOpcode,
                    ShaderBytecodeType.Bool,
                    bool,
                    value.label!!,
                    method.cls.builder.getConstant(ShaderConstant(ShaderBytecodeType.INT, listOf(0)))
                )
            }
        )

        val body = method.branches[block.index + 1]!!.label//ShaderLabelNode()
        insns.add(ShaderInsnNode(OP_SELECTION_MERGE, target, SELECTION_CONTROL_NONE))
        insns.add(ShaderInsnNode(OP_BRANCH_CONDITIONAL, bool, body, target))

        ended = true
    }

    fun jump(comparisonOpcode: Int, target: LabelNode, right: ShaderLabelNode) {
        val target = method.branches[method.cfg.blocksByInsn[target]!!]!!.label
        val left = frame.pop()
        val bool = ShaderLabelNode()
        insns.add(ShaderInsnNode(comparisonOpcode, ShaderBytecodeType.Bool, bool, left.label!!, right))
        val body = method.branches[block.index + 1]!!.label//ShaderLabelNode()
        insns.add(ShaderInsnNode(OP_SELECTION_MERGE, target, SELECTION_CONTROL_NONE))
        insns.add(ShaderInsnNode(OP_BRANCH_CONDITIONAL, bool, body, target))

        ended = true
    }

    fun jump(target: LabelNode) {
        val target = method.branches[method.cfg.blocksByInsn[target]!!]!!.label
        insns.add(ShaderInsnNode(OP_BRANCH, target))

        ended = true
    }

    fun const(type: ShaderBytecodeType, value: Any) {
        frame.push(ShaderStackValue.Constant(method.cls.builder, ShaderConstant(type, listOf(value))))
    }

    fun cast(opcode: Int, to: ShaderBytecodeType) {
        val v = frame.pop()

        if (v is ShaderStackValue.Constant) {
            v.const.tryCast(to)?.let {
                frame.push(ShaderStackValue.Constant(v.builder, it))
                return
            }
        }

        val r = ShaderLabelNode()
        insns.add(ShaderInsnNode(opcode, to, r, v.label))
        frame.push(ShaderStackValue.Label(r, to))
    }

    fun math(opcode: Int, result: ShaderBytecodeType) {
        val b = frame.pop().label!!
        val a = frame.pop().label!!
        val r = ShaderLabelNode()
        insns.add(ShaderInsnNode(opcode, result, r, a, b))
        frame.push(ShaderStackValue.Label(r, result))
    }

    fun mathUnary(opcode: Int, result: ShaderBytecodeType) {
        val a = frame.pop().label!!
        val r = ShaderLabelNode()
        insns.add(ShaderInsnNode(opcode, result, r, a))
        frame.push(ShaderStackValue.Label(r, result))
    }

    fun compileNext() {
        val insn = iterator.next()

        when (insn.opcode) {
            -1 -> {}

            Opcodes.NOP -> insns.add(
                ShaderInsnNode(
                    OP_NO_OP
                )
            )

            Opcodes.ACONST_NULL -> throw JavaShaderCompilationException("Nullability isn't supported")

            Opcodes.ICONST_M1 -> const(ShaderBytecodeType.INT, -1)
            Opcodes.ICONST_0 -> const(ShaderBytecodeType.INT, 0)
            Opcodes.ICONST_1 -> const(ShaderBytecodeType.INT, 1)
            Opcodes.ICONST_2 -> const(ShaderBytecodeType.INT, 2)
            Opcodes.ICONST_3 -> const(ShaderBytecodeType.INT, 3)
            Opcodes.ICONST_4 -> const(ShaderBytecodeType.INT, 4)
            Opcodes.ICONST_5 -> const(ShaderBytecodeType.INT, 5)

            Opcodes.LCONST_0 -> const(ShaderBytecodeType.LONG, 0L)
            Opcodes.LCONST_1 -> const(ShaderBytecodeType.LONG, 1L)

            Opcodes.FCONST_0 -> const(ShaderBytecodeType.FLOAT, 0f)
            Opcodes.FCONST_1 -> const(ShaderBytecodeType.FLOAT, 1f)
            Opcodes.FCONST_2 -> const(ShaderBytecodeType.FLOAT, 2f)

            Opcodes.DCONST_0 -> const(ShaderBytecodeType.DOUBLE, 0.0)
            Opcodes.DCONST_1 -> const(ShaderBytecodeType.DOUBLE, 1.0)

            Opcodes.BIPUSH, Opcodes.SIPUSH -> const(
                ShaderBytecodeType.INT, (insn as IntInsnNode).operand)
            Opcodes.LDC -> when (val const = (insn as LdcInsnNode).cst) {
                is Boolean -> const(ShaderBytecodeType.Bool, const)
                is Byte -> const(ShaderBytecodeType.BYTE, const)
                is Short -> const(ShaderBytecodeType.SHORT, const)
                is Int -> const(ShaderBytecodeType.INT, const)
                is Long -> const(ShaderBytecodeType.LONG, const)
                is Float -> const(ShaderBytecodeType.FLOAT, const)
                is Double -> const(ShaderBytecodeType.DOUBLE, const)
                is String -> frame.push(ShaderStackValue.StringConstant(const))
                else -> throw JavaShaderCompilationException("Unsupported constant $const")
            }

            Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD -> {
                val local = frame.getLocal((insn as VarInsnNode).`var`)!!

                if (local is ShaderLocal.This) {
                    frame.push(ShaderStackValue.This)
                } else {
                    frame.push(local.load(method)!!)
                }
            }
            Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD -> {
                val index = frame.pop().label!!
                val array = frame.pop() as ShaderStackValue.Array

                val pointer = ShaderLabelNode()
                val value = ShaderLabelNode()
                insns.add(ShaderInsnNode(OP_ACCESS_CHAIN, array.variable.type, pointer, array.variable.label, index))
                insns.add(ShaderInsnNode(OP_LOAD, array.variable.type.rootType, value, pointer))
                frame.push(ShaderStackValue.Label(value, array.variable.type.rootType))
            }

            Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE -> {
                insn as VarInsnNode

                when (val value = frame.pop()) {
                    is ShaderStackValue.Array -> {
                        val local = frame.getOrLoadLocal(insn.`var`, value.type)

                        if (local !is ShaderLocal.NewArray) {
                            TODO("reassigning arrays?")
                        }

                        if (value.variable.label.name == null) {
                            value.variable.label.name = local.name
                        }

                        frame.setLocal(insn.`var`, ShaderLocal.Variable(insns, value.variable))
                    }
                    // TODO
                    /*
                    is ShaderStackValue.LoadVariable -> if (value.variable.type.type is ShaderBytecodeType.Vector) {
                        throw JavaShaderCompilationException("Cannot store a mutable ${value.variable.type.type} value from one variable in another, since joml vectors are mutable while glsl vectors are immutable.")
                    }
                     */
                    else -> frame.getOrLoadLocal(insn.`var`, value.type!!).store(method, value)!!
                }
            }

            Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.SASTORE -> {
                val value = frame.pop().label!!
                val index = frame.pop().label!!
                val array = frame.pop() as ShaderStackValue.Array

                val pointer = ShaderLabelNode()
                insns.add(ShaderInsnNode(OP_ACCESS_CHAIN, array.variable.type, pointer, array.variable.label, index))
                insns.add(ShaderInsnNode(OP_STORE, pointer, value))
            }

            Opcodes.POP -> frame.pop()
            Opcodes.POP2 -> {
                frame.pop()
                frame.pop()
            }
            Opcodes.DUP -> frame.dup()
            Opcodes.DUP_X1, Opcodes.DUP_X2, Opcodes.DUP2, Opcodes.DUP2_X1, Opcodes.DUP2_X2 -> TODO("DUP opcode ${insn.opcode}")
            Opcodes.SWAP -> frame.swap()

            Opcodes.IADD -> math(OP_I_ADD, ShaderBytecodeType.INT)
            Opcodes.LADD -> math(OP_I_ADD, ShaderBytecodeType.LONG)
            Opcodes.FADD -> math(OP_F_ADD, ShaderBytecodeType.FLOAT)
            Opcodes.DADD -> math(OP_F_ADD, ShaderBytecodeType.DOUBLE)

            Opcodes.ISUB -> math(OP_I_SUB, ShaderBytecodeType.INT)
            Opcodes.LSUB -> math(OP_I_SUB, ShaderBytecodeType.LONG)
            Opcodes.FSUB -> math(OP_F_SUB, ShaderBytecodeType.FLOAT)
            Opcodes.DSUB -> math(OP_F_SUB, ShaderBytecodeType.DOUBLE)

            Opcodes.IMUL -> math(OP_I_MUL, ShaderBytecodeType.INT)
            Opcodes.LMUL -> math(OP_I_MUL, ShaderBytecodeType.LONG)
            Opcodes.FMUL -> math(OP_F_MUL, ShaderBytecodeType.FLOAT)
            Opcodes.DMUL -> math(OP_F_MUL, ShaderBytecodeType.DOUBLE)

            Opcodes.IDIV -> math(OP_S_DIV, ShaderBytecodeType.INT)
            Opcodes.LDIV -> math(OP_S_DIV, ShaderBytecodeType.LONG)
            Opcodes.FDIV -> math(OP_F_DIV, ShaderBytecodeType.FLOAT)
            Opcodes.DDIV -> math(OP_F_DIV, ShaderBytecodeType.DOUBLE)

            Opcodes.IREM -> math(OP_S_REM, ShaderBytecodeType.INT)
            Opcodes.LREM -> math(OP_S_REM, ShaderBytecodeType.LONG)
            Opcodes.FREM -> math(OP_F_REM, ShaderBytecodeType.FLOAT)
            Opcodes.DREM -> math(OP_F_REM, ShaderBytecodeType.DOUBLE)

            Opcodes.INEG -> mathUnary(OP_S_NEGATE, ShaderBytecodeType.INT)
            Opcodes.LNEG -> mathUnary(OP_S_NEGATE, ShaderBytecodeType.LONG)
            Opcodes.FNEG -> mathUnary(OP_F_NEGATE, ShaderBytecodeType.FLOAT)
            Opcodes.DNEG -> mathUnary(OP_F_NEGATE, ShaderBytecodeType.DOUBLE)

            Opcodes.ISHL -> math(OP_SHIFT_LEFT_LOGICAL, ShaderBytecodeType.INT)
            Opcodes.LSHL -> math(OP_SHIFT_LEFT_LOGICAL, ShaderBytecodeType.LONG)
            Opcodes.ISHR -> math(OP_SHIFT_RIGHT_ARITHMETIC, ShaderBytecodeType.INT)
            Opcodes.LSHR -> math(OP_SHIFT_RIGHT_ARITHMETIC, ShaderBytecodeType.LONG)
            Opcodes.IUSHR -> math(OP_SHIFT_RIGHT_LOGICAL, ShaderBytecodeType.INT)
            Opcodes.LUSHR -> math(OP_SHIFT_RIGHT_LOGICAL, ShaderBytecodeType.LONG)
            Opcodes.IAND -> math(OP_BITWISE_AND, ShaderBytecodeType.INT)
            Opcodes.LAND -> math(OP_BITWISE_AND, ShaderBytecodeType.LONG)
            Opcodes.IOR -> math(OP_BITWISE_OR, ShaderBytecodeType.INT)
            Opcodes.LOR -> math(OP_BITWISE_OR, ShaderBytecodeType.LONG)
            Opcodes.IXOR -> math(OP_BITWISE_XOR, ShaderBytecodeType.INT)
            Opcodes.LXOR -> math(OP_BITWISE_XOR, ShaderBytecodeType.LONG)

            Opcodes.IINC -> {
                insn as IincInsnNode
                val value = method.cls.builder.getConstant(ShaderConstant(ShaderBytecodeType.INT, listOf(insn.incr)))
                val local = frame.getOrLoadLocal(insn.`var`, ShaderBytecodeType.INT)
                val temp = local.load(method)!!
                val result = ShaderLabelNode()

                insns.add(ShaderInsnNode(OP_I_ADD, ShaderBytecodeType.INT /* TODO */, result, temp.label!!, value))
                local.store(method, ShaderStackValue.Label(result, ShaderBytecodeType.INT))!!
            }

            Opcodes.I2L -> cast(OP_S_CONVERT, ShaderBytecodeType.LONG)
            Opcodes.I2F -> cast(OP_CONVERT_S_TO_F, ShaderBytecodeType.FLOAT)
            Opcodes.I2D -> cast(OP_CONVERT_S_TO_F, ShaderBytecodeType.DOUBLE)
            Opcodes.L2I -> cast(OP_S_CONVERT, ShaderBytecodeType.INT)
            Opcodes.L2F -> cast(OP_CONVERT_S_TO_F, ShaderBytecodeType.FLOAT)
            Opcodes.L2D -> cast(OP_CONVERT_S_TO_F, ShaderBytecodeType.DOUBLE)
            Opcodes.F2I -> cast(OP_CONVERT_F_TO_S, ShaderBytecodeType.INT)
            Opcodes.F2L -> cast(OP_CONVERT_F_TO_S, ShaderBytecodeType.LONG)
            Opcodes.F2D -> cast(OP_F_CONVERT, ShaderBytecodeType.DOUBLE)
            Opcodes.D2I -> cast(OP_CONVERT_F_TO_S, ShaderBytecodeType.INT)
            Opcodes.D2L -> cast(OP_CONVERT_F_TO_S, ShaderBytecodeType.LONG)
            Opcodes.D2F -> cast(OP_F_CONVERT, ShaderBytecodeType.FLOAT)
            Opcodes.I2B -> cast(OP_S_CONVERT, ShaderBytecodeType.BYTE)
            Opcodes.I2C, Opcodes.I2S -> cast(OP_S_CONVERT, ShaderBytecodeType.SHORT)

            Opcodes.LCMP, Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG -> frame.push(ShaderStackValue.Comparison(insn.opcode, frame.pop(), frame.pop()))

            Opcodes.IFEQ -> jump(OP_I_NOT_EQUAL, OP_F_ORD_NOT_EQUAL, (insn as JumpInsnNode).label)
            Opcodes.IFNE -> jump(OP_I_EQUAL, OP_F_ORD_EQUAL, (insn as JumpInsnNode).label)
            Opcodes.IFLT -> jump(OP_S_GREATER_THAN_EQUAL, OP_F_ORD_GREATER_THAN_EQUAL, (insn as JumpInsnNode).label)
            Opcodes.IFGE -> jump(OP_S_LESS_THAN, OP_F_ORD_LESS_THAN, (insn as JumpInsnNode).label)
            Opcodes.IFGT -> jump(OP_S_LESS_THAN_EQUAL, OP_F_ORD_LESS_THAN_EQUAL, (insn as JumpInsnNode).label)
            Opcodes.IFLE -> jump(OP_S_GREATER_THAN, OP_F_ORD_GREATER_THAN, (insn as JumpInsnNode).label)

            Opcodes.IF_ICMPEQ -> jump(OP_I_NOT_EQUAL, (insn as JumpInsnNode).label, frame.pop().label!!)
            Opcodes.IF_ICMPNE -> jump(OP_I_EQUAL, (insn as JumpInsnNode).label, frame.pop().label!!)
            Opcodes.IF_ICMPLT -> jump(OP_S_GREATER_THAN_EQUAL, (insn as JumpInsnNode).label, frame.pop().label!!)
            Opcodes.IF_ICMPGE -> jump(OP_S_LESS_THAN, (insn as JumpInsnNode).label, frame.pop().label!!)
            Opcodes.IF_ICMPGT -> jump(OP_S_LESS_THAN_EQUAL, (insn as JumpInsnNode).label, frame.pop().label!!)
            Opcodes.IF_ICMPLE -> jump(OP_S_GREATER_THAN, (insn as JumpInsnNode).label, frame.pop().label!!)

            Opcodes.GOTO -> jump((insn as JumpInsnNode).label)

            // TODO comparison ops
            // TODO jump ops
            // TODO RET
            // TODO switches

            Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN -> {
                insns.add(ShaderInsnNode(OP_RETURN_VALUE, frame.pop().label!!))
                ended = true
            }
            Opcodes.RETURN -> {
                insns.add(ShaderInsnNode(OP_RETURN))
                ended = true
            }

            Opcodes.GETFIELD -> {
                insn as FieldInsnNode

                method.cls.getTypeHandler(Type.getObjectType(insn.owner))?.let {
                    it.handleFieldOp(this, insn)
                    return
                }

                val target = frame.pop()

                if (target == ShaderStackValue.This) {
                    method.cls.variables[insn.name]?.let { v ->
                        frame.push(ShaderStackValue.LoadVariable(insns, v))
                        return
                    }
                }

                TODO("${insn.owner} ${insn.name} ${insn.desc}")
            }
            Opcodes.PUTFIELD -> {
                insn as FieldInsnNode

                method.cls.getTypeHandler(Type.getObjectType(insn.owner))?.let {
                    it.handleFieldOp(this, insn)
                    return
                }

                val value = frame.pop()
                val target = frame.pop()

                if (target == ShaderStackValue.This) {
                    method.cls.variables[insn.name]?.let { v ->
                        insns.add(ShaderInsnNode(OP_STORE, v.label, value.label!!))
                        return
                    }
                }

                TODO("${insn.owner} ${insn.name} ${insn.desc}")
            }
            // TODO static fields

            Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE, Opcodes.INVOKEDYNAMIC -> {
                insn as MethodInsnNode

                if (insn.owner == method.cls.node.name) {
                    val node = MethodPointer.method().name(insn.name).desc(insn.desc).findOrThrow(method.cls.node)
                    val target = method.cls.methods[node]!!
                    val args = Array(Type.getArgumentCount(insn.desc)) { frame.pop().label!! }.reversedArray()

                    if (frame.pop() != ShaderStackValue.This) {
                        throw AssertionError()
                    }

                    val result = ShaderLabelNode()
                    insns.add(target.call(result, *args))

                    if (target.type.returnType != ShaderBytecodeType.Void) {
                        frame.push(ShaderStackValue.Label(result, target.type.returnType))
                    }

                    return
                }

                method.cls.getTypeHandler(Type.getObjectType(insn.owner))?.let {
                    it.handleMethodCall(this, insn)
                    return
                }
            }

            Opcodes.NEW -> frame.pushNewObject()
            Opcodes.NEWARRAY -> {
                val length = frame.pop()

                if (length !is ShaderStackValue.Constant) {
                    throw JavaShaderCompilationException("Cannot create arrays of dynamic size")
                }

                val type = when ((insn as IntInsnNode).operand) {
                    Opcodes.T_BOOLEAN -> ShaderBytecodeType.Bool
                    Opcodes.T_BYTE -> ShaderBytecodeType.BYTE
                    Opcodes.T_CHAR, Opcodes.T_SHORT -> ShaderBytecodeType.SHORT
                    Opcodes.T_INT -> ShaderBytecodeType.INT
                    Opcodes.T_LONG -> ShaderBytecodeType.LONG
                    Opcodes.T_FLOAT -> ShaderBytecodeType.FLOAT
                    Opcodes.T_DOUBLE -> ShaderBytecodeType.DOUBLE
                    else -> throw AssertionError()
                }
                val variable = ShaderVariable(
                    ShaderBytecodeType.Pointer(
                        STORAGE_CLASS_FUNCTION,
                        ShaderBytecodeType.Array(type, length.const.value.first() as Int)
                    )
                )
                frame.push(ShaderStackValue.Array(variable))
                insns.add(ShaderInsnNode(OP_VARIABLE, variable.type, variable.label, variable.type.storageClass, variable.initializer))
            }
            // TODO ANEWARRAY
            // TODO ARRAYLENGTH
            // TODO ATHROW
            Opcodes.CHECKCAST -> method.cls.getTypeHandler(Type.getObjectType((insn as TypeInsnNode).desc))?.handleCastFrom(this, frame.peek()!!)
            // TODO INSTANCEOF
            // TODO synchronization
            // TODO MULTIANEWARRAY
            // TODO null jumps

            else -> TODO("unsupported opcode ${insn.opcode}")
        }
    }
}