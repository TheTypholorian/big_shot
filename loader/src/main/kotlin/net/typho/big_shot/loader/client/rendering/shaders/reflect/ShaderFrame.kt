package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.OP_VARIABLE
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.STORAGE_CLASS_FUNCTION
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderInsnNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderLabelNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderVariable

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

    fun loadLocal(id: Int, type: ShaderBytecodeType, old: ShaderLocal?): ShaderLocal {
        if (type is ShaderBytecodeType.Pointer) {
            throw IllegalArgumentException()
        }

        if (old?.type == type) {
            return old
        }

        val name = branch.currentInsn?.let { insn ->
            branch.method.node.localVariables
                ?.filter { it.index == id }
                ?.map { it to branch.method.node.instructions.indexOf(it.end) }
                ?.sortedBy { (local, index) -> index }
                ?.firstOrNull { (local, index) -> index >= branch.method.node.instructions.indexOf(insn) }?.first?.name
        }  ?: "var${branch.method.localNameCounter++}"

        return if (type is ShaderBytecodeType.Array) ShaderLocal.NewArray(type, name) else branch.localDelegate.createLocalVariable(type, ShaderLabelNode(name))
    }

    fun getOrLoadLocal(id: Int, type: ShaderBytecodeType): ShaderLocal {
        val local = locals[id]
        val new = loadLocal(id, type, local)
        locals[id] = new
        return new
    }

    fun clear() {
        locals.fill(null)
        stack.fill(null)
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
        stack[stackIndex] = null // TODO
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

    fun peek() = stack.lastOrNull()

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
        fun merge(result: ShaderMethodBranch, branches: List<ShaderMethodBranch>, nested: Boolean = false): ShaderFrame {
            if (branches.isEmpty()) {
                return ShaderFrame(result, null)
            }

            branches.forEach { it.compile() }

            if (branches.windowed(2) { (a, b) -> a.frame == b.frame }.all { it }) {
                return ShaderFrame(result, branches.first().frame)
            }

            println("Cannot merge frames")
            branches.forEach { println(it.frame) }
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

            val result = merge(result, branches, true)

            if (!nested) {
                branches.forEach { it.localDelegate = it }
            }

            return result
        }
    }
}