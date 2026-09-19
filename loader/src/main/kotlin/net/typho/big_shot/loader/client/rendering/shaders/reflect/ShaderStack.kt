package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType

class ShaderStack(
    @JvmField
    val branch: ShaderMethodBranch,
    parent: ShaderStack?
) {
    private val stack = mutableListOf<ShaderStackValue>()

    init {
        parent?.let { stack.addAll(it.stack) }
    }

    fun clear() {
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