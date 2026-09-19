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
    private val branches = mutableMapOf<Int, ShaderMethodBranch>()
    @JvmField
    var localNameCounter = 0
    @JvmField
    var newObjectIdCounter = 0

    fun getOrLoadBranch(id: Int): ShaderMethodBranch = branches.computeIfAbsent(id) {
        val block = cfg.blocksByIndex[it]!!
        val branch = ShaderMethodBranch(this, block)

        if (block.previous.isEmpty()) {
            if (id != 0) {
                throw AssertionError()
            }

            branch.frame = ShaderFrame(branch, null)
        } else {
            branch.frame = ShaderFrame.merge(branch, block.previous.map { getOrLoadBranch(it) })
        }

        branch
    }

    fun compile() {
        cfg = ControlFlowGraph.build(node.instructions)
        branches.clear()
        method.insns.clear()
        localNameCounter = 0
        newObjectIdCounter = 0
        static = node.access and Opcodes.ACC_STATIC != 0

        println(cfg.blocks)

        val mainBranch = getOrLoadBranch(0)

        if (!static) {
            mainBranch.frame.setLocal(0, ShaderLocal.This)
        }

        Type.getArgumentTypes(node.desc).forEachIndexed { index, type ->
            val type = ShaderBytecodeType.convertJavaType(type)
            val label = ShaderLabelNode()
            method.insns.add(ShaderInsnNode(OP_FUNCTION_PARAMETER, type, label))
            mainBranch.frame.setLocal(if (static) index else index + 1, ShaderLocal.Argument(label, type))
        }

        while (branches.values.any { !it.compiled }) {
            branches.values.toList().forEach { it.compile() }
        }

        method.insns.addAll(branches.values)

        if (!mainBranch.frame.isStackEmpty()) {
            throw JavaShaderCompilationException("Stack is not empty at the end of method ${node.name}, still contains ${mainBranch.frame}")
        }
    }
}