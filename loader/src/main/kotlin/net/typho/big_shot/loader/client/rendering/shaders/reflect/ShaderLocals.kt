package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.OP_VARIABLE
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.STORAGE_CLASS_FUNCTION
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderInsnNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderLabelNode
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderVariable

class ShaderLocals(
    @JvmField
    val branch: ShaderMethodBranch,
    parent: ShaderLocals?
) {
    private val locals = mutableMapOf<Int, ShaderLocal>()

    init {
        parent?.let { locals.putAll(it.locals) }
    }

    operator fun get(id: Int) = locals[id]

    operator fun set(id: Int, local: ShaderLocal) {
        locals[id] = local
    }

    fun load(id: Int, type: ShaderBytecodeType, old: ShaderLocal?): ShaderLocal {
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

    fun getOrLoad(id: Int, type: ShaderBytecodeType) = locals.compute(id) { key, local -> load(id, type, local) }!!
}