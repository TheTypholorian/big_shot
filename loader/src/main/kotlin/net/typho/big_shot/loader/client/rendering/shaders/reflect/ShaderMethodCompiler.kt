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

    fun getOrLoadBranch(id: Int): ShaderMethodBranch {
        branches[id]?.let { return it }

        val block = cfg.blocksByIndex[id]!!
        val branch = ShaderMethodBranch(this, block)
        branches[id] = branch

        if (id == 0) {
            branch.rootBranch = branch
        } else {
            branch.rootBranch = getOrLoadBranch(0)
        }

        println("Initializing frame for branch $id")
        if (block.previous.isEmpty()) {
            if (id != 0) {
                throw AssertionError()
            }

            branch.frame = ShaderFrame(branch, null)
        } else {
            println("Loading previous branches ${block.previous}")
            var previous = block.previous.map { getOrLoadBranch(it) }

            while (previous.all { it.initializing }) {
                previous = previous.flatMap { it.block.previous }.map { getOrLoadBranch(it) }
                println("Going back to ${previous.map { it.block.index }}")

                if (previous.isEmpty()) {
                    println("Initialized frame for branch $id to empty")
                    branch.frame = ShaderFrame.merge(branch, previous)
                    branch.initializing = false
                    return branch
                }
            }

            if (previous.none { it.initializing }) {
                println("\tMerging $id ${block.previous}")
                branch.frame = ShaderFrame.merge(branch, previous)
            } else {
                val remaining = previous.filter { !it.initializing }

                if (!ShaderFrame.canMerge(remaining)) {
                    println("Warning: Remaining branches ${remaining.map { it.block.index }} cannot be merged")
                    branch.frame = ShaderFrame(branch, cfg.getCommonParent(previous.map { it.block })?.let { getOrLoadBranch(it.index).frame })
                } else {
                    branch.frame = ShaderFrame(branch, remaining.first().frame)
                }
            }
        }
        println("Initialized frame for branch $id")

        branch.initializing = false
        return branch
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

        while (branches.values.any { it.queue.isNotEmpty() }) {
            branches.values.toList().forEach { it.compile() }
        }

        method.insns.addAll(branches.values)
    }
}