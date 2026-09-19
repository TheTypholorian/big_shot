package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.asm_util.cfg.ControlFlowGraph
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LabelNode
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
    val jumpTargets = mutableMapOf<LabelNode, ShaderLabelNode>()
    @JvmField
    var localNameCounter = 0
    @JvmField
    var newObjectIdCounter = 0

    fun compile() {
        cfg = ControlFlowGraph.build(node.instructions)
        jumpTargets.clear()
        method.insns.clear()
        localNameCounter = 0
        newObjectIdCounter = 0

        println(cfg.blocks)

        val branch = ShaderMethodBranch(this, cfg.blocks.first(), null)

        static = node.access and Opcodes.ACC_STATIC != 0

        if (!static) {
            branch.locals[0] = ShaderLocal.This
        }

        Type.getArgumentTypes(node.desc).forEachIndexed { index, type ->
            val type = ShaderBytecodeType.convertJavaType(type)
            val label = ShaderLabelNode()
            method.insns.add(ShaderInsnNode(OP_FUNCTION_PARAMETER, type, label))
            branch.locals[if (static) index else index + 1] = ShaderLocal.Argument(label, type)
        }

        branch.compile()
        method.insns.addAll(branch.insns) // TODO

        if (!branch.stack.isEmpty()) {
            throw JavaShaderCompilationException("Stack is not empty at the end of method ${node.name}")
        }
    }
}