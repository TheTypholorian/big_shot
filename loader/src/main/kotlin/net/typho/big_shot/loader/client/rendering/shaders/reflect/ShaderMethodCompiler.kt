package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.asm_util.cfg.ControlFlowGraph
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

class ShaderMethodCompiler(
    @JvmField
    val cls: JavaShaderCompiler,
    @JvmField
    val node: MethodNode,
    @JvmField
    val method: ShaderMethod<IShaderInsn>
) {
    lateinit var cfg: ControlFlowGraph
        private set
    var static: Boolean = false
        private set
    @JvmField
    val branches = mutableMapOf<Int, ShaderMethodBranch>()
    @JvmField
    var localNameCounter = 0
    @JvmField
    var newObjectIdCounter = 0

    fun compile() {
        cfg = ControlFlowGraph.build(node.instructions)
        branches.clear()
        method.insns.clear()
        localNameCounter = 0
        newObjectIdCounter = 0
        static = node.access and Opcodes.ACC_STATIC != 0

        println(cfg.blocks)

        for (block in cfg.blocks) {
            val branch = ShaderMethodBranch(this, block, null) // TODO
            branches[block.index] = branch
            method.insns.add(branch)
        }

        val mainBranch = branches[0]!!

        if (!static) {
            mainBranch.locals[0] = ShaderLocal.This
        }

        Type.getArgumentTypes(node.desc).forEachIndexed { index, type ->
            val type = ShaderBytecodeType.convertJavaType(type)
            val label = ShaderLabelNode()
            method.insns.add(ShaderInsnNode(OP_FUNCTION_PARAMETER, type, label))
            mainBranch.locals[if (static) index else index + 1] = ShaderLocal.Argument(label, type)
        }

        branches.values.forEach { it.compile() }

        if (!mainBranch.stack.isEmpty()) {
            throw JavaShaderCompilationException("Stack is not empty at the end of method ${node.name}")
        }
    }
}