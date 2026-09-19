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
    private val locals = mutableMapOf<Int, ShaderLocal>()
    private val stack = mutableListOf<ShaderStackValue>()

    init {
        parent?.let {
            locals.putAll(it.locals)
            stack.addAll(it.stack)
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

        val name = branch.method.node.localVariables
            ?.filter { it.index == id }
            ?.map { it to branch.method.node.instructions.indexOf(it.end) }
            ?.sortedBy { (local, index) -> index }
            ?.firstOrNull { (local, index) -> index >= branch.iterator.previousIndex() }?.first?.name ?: "var${branch.method.localNameCounter++}"

        return if (type is ShaderBytecodeType.Array) {
            ShaderLocal.NewArray(type, name)
        } else {
            val variable =
                ShaderVariable(ShaderBytecodeType.Pointer(STORAGE_CLASS_FUNCTION, type), ShaderLabelNode(name))
            branch.insns.add(
                ShaderInsnNode(
                    OP_VARIABLE,
                    variable.type,
                    variable.label,
                    variable.type.storageClass,
                    variable.initializer
                )
            )
            ShaderLocal.Variable(branch.insns, variable)
        }
    }

    fun getOrLoadLocal(id: Int, type: ShaderBytecodeType) = locals.compute(id) { key, local -> loadLocal(id, type, local) }!!

    fun clear() {
        locals.clear()
        stack.clear()
    }

    fun push(value: ShaderStackValue) {
        stack.add(value)
        println("${stack.size} pushed $value")
    }

    fun pushNewObject() {
        push(ShaderStackValue.NewObject(branch.method.newObjectIdCounter++))
    }

    fun pop() = stack.removeLast().also { println("${stack.size} popped $it") }

    fun popVectorComponents(type: ShaderBytecodeType.Vector): Array<ShaderStackValue> = Array(type.componentCount) { pop() }.reversedArray()

    fun dup() {
        stack.add(stack.last().also { println("${stack.size + 1} dup $it") })
    }

    fun swap() {
        val last = stack.removeLast()
        stack.add(stack.size - 1, last)
    }

    fun peek() = stack.lastOrNull()

    fun replace(value: ShaderStackValue, with: ShaderStackValue) {
        stack.replaceAll { if (it == value) with else it }
        println("${stack.size} replaced $value with $with")
    }

    fun isEmpty() = stack.isEmpty()
}