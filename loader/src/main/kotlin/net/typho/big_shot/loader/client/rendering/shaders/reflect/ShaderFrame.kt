package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderLabelNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.VarInsnNode
import kotlin.to

class ShaderFrame(
    @JvmField
    val branch: ShaderMethodBranch,
    parent: ShaderFrame?
) {
    private val locals = arrayOfNulls<ShaderLocal>(branch.method.node.maxLocals)
    private val stack = arrayOfNulls<ShaderStackValue>(branch.method.node.maxStack)
    private var stackIndex = 0

    init {
        parent?.let {
            it.locals.copyInto(locals)
            it.stack.copyInto(stack)
            stackIndex = it.stackIndex
        }
    }

    fun getLocal(id: Int) = locals[id]

    fun setLocal(id: Int, local: ShaderLocal) {
        locals[id] = local
    }

    fun localMatches(id: Int, type: ShaderBytecodeType): Boolean {
        if (type is ShaderBytecodeType.Pointer) {
            throw IllegalArgumentException()
        }

        return locals[id]?.type == type
    }

    fun getOrLoadLocal(id: Int, type: ShaderBytecodeType): ShaderLocal {
        return getOrLoadLocal(id, type) {
            val name = getLocalName(branch.currentInsn as? VarInsnNode, branch.method)
            if (type is ShaderBytecodeType.Array) ShaderLocal.NewArray(type, name) else branch.createLocalVariable(type, ShaderLabelNode(name))
        }
    }

    fun getOrLoadLocal(id: Int, type: ShaderBytecodeType, supplier: () -> ShaderLocal): ShaderLocal {
        if (localMatches(id, type)) {
            return locals[id]!!
        }

        val new = supplier()
        locals[id] = new
        return new
    }

    fun clear() {
        locals.fill(null)
        stack.fill(null)
        stackIndex = 0
    }

    fun push(value: ShaderStackValue) {
        stack[stackIndex++] = value
        println("$stackIndex pushed $value")
    }

    fun pushNewObject() {
        push(ShaderStackValue.NewObject(branch.method.newObjectIdCounter++))
    }

    fun pop(): ShaderStackValue {
        stackIndex--
        val value = stack[stackIndex]
        println("$stackIndex popped $value")
        stack[stackIndex] = null
        return value!!
    }

    fun popVectorComponents(type: ShaderBytecodeType.Vector): Array<ShaderStackValue> = Array(type.componentCount) { pop() }.reversedArray()

    fun dup() {
        val v = stack[stackIndex - 1].also { println("${stackIndex + 1} dup $it") }!!
        stack[stackIndex++] = v
    }

    fun swap() {
        val last = stack[stackIndex - 1]
        stack[stackIndex - 1] = stack[stackIndex - 2]
        stack[stackIndex - 2] = last
    }

    fun peek() = stack[stackIndex - 1]

    fun replace(value: ShaderStackValue, with: ShaderStackValue) {
        repeat(stackIndex) {
            if (stack[it]!! == value) {
                stack[it] = with
            }
        }

        println("$stackIndex replaced $value with $with")
    }

    fun isStackEmpty() = stackIndex == 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShaderFrame

        if (!locals.contentEquals(other.locals)) return false
        if (!stack.contentEquals(other.stack)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = locals.contentHashCode()
        result = 31 * result + stack.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Locals=[${locals.joinToString(separator = "") { if (it == null) "." else if (it == ShaderLocal.This) "{this}" else it.type?.toFrameString() ?: "?" }}] Stack=[${stack.joinToString(separator = "") { if (it == null) "." else if (it == ShaderStackValue.This) "{this}" else it.type?.toFrameString() ?: "?" }}]"
    }

    companion object {
        @JvmStatic
        fun getLocalName(insn: VarInsnNode?, method: ShaderMethodCompiler): String {
            return insn?.let {
                method.node.localVariables
                    ?.filter { it.index == insn.`var` }
                    ?.map { it to method.node.instructions.indexOf(it.end) }
                    ?.sortedBy { (local, index) -> index }
                    ?.firstOrNull { (local, index) -> index >= method.node.instructions.indexOf(insn) }?.first?.name
            } ?: "var${method.localNameCounter++}"
        }

        @JvmStatic
        fun canMerge(branches: List<ShaderMethodBranch>): Boolean {
            if (branches.isEmpty()) {
                throw IllegalArgumentException()
            }

            branches.forEach { it.compile() }

            return branches.windowed(2) { (a, b) -> a.frame == b.frame }.all { it }
        }

        @JvmStatic
        fun merge(next: ShaderMethodBranch, branches: List<ShaderMethodBranch>): ShaderFrame {
            if (branches.isEmpty()) {
                return ShaderFrame(next, null)
            }

            if (!canMerge(branches)) {
                val parent by lazy {
                    next.method.cfg.getCommonParent(branches.map { it.block })!!
                }

                do {
                    println("Cannot merge frames")
                    branches.forEach { println(it.frame) }
                    /*
                    branches.windowed(2) { (a, b) ->
                        println("Locals")
                        a.frame.locals.zip(b.frame.locals).forEach { (a, b) ->
                            println("\t$a == $b: ${a == b}")
                        }

                        println("Stack")
                        a.frame.stack.zip(b.frame.stack).forEach { (a, b) ->
                            println("\t$a == $b: ${a == b}")
                        }
                    }
                     */

                    var insn: AbstractInsnNode

                    do {
                        insn = next.queue.removeFirst()
                    } while (insn.opcode == -1)

                    println("shifting $insn from ${next.block.index} to ${branches.map { it.block.index }}")

                    when (insn.opcode) {
                        // TODO
                        Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE -> {
                            insn as VarInsnNode

                            val values = branches.map { it.frame.pop() }

                            if (values.windowed(2) { (a, b) -> a.type == b.type }.any { !it }) {
                                throw AssertionError()
                            }

                            val type = values.first().type!!

                            if (type is ShaderBytecodeType.Array) {
                                TODO()
                            }

                            val matches = branches.map { it.frame.localMatches(insn.`var`, type) }

                            if (matches.all { it }) {
                                branches.forEach { it.queue.add(insn) }
                            } else if (matches.none { it }) {
                                val local = next.method.getOrLoadBranch(parent.index).createLocalVariable(type, ShaderLabelNode(getLocalName(insn, next.method)))

                                branches.zip(values).forEach { (branch, value) ->
                                    branch.frame.getOrLoadLocal(insn.`var`, type) { local }.store(branch.insns, branch, value)
                                }
                            } else {
                                throw AssertionError(matches)
                            }
                        }
                        else -> branches.forEach { it.queue.add(insn) }
                    }
                } while (!canMerge(branches))
            }

            /*
            if (result.queue.isEmpty()) {
                TODO()
            }

            if (!nested) {
                result.method.cfg.getCommonParent(branches.map { it.block })?.let { parent ->
                    val branch = result.method.getOrLoadBranch(parent.index)
                    branches.forEach { it.localDelegate = branch }
                }
            }

            val insn = result.queue.removeFirst()
            println("shifting $insn")

            branches.forEach { it.queue.add(insn) }
             */

            return ShaderFrame(next, branches.first().frame)
        }
    }
}