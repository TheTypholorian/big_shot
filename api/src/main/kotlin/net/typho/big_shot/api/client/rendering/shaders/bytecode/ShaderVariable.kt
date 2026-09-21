package net.typho.big_shot.api.client.rendering.shaders.bytecode

data class ShaderVariable @JvmOverloads constructor(
    @JvmField
    val type: ShaderBytecodeType.Pointer,
    @JvmField
    val label: ShaderLabelNode = ShaderLabelNode(),
    @JvmField
    val initializer: ShaderLabelNode? = null,
    @JvmField
    val location: Int? = null
) {
    override fun toString(): String {
        return buildString {
            append("ShaderVariable(type=$type")
            label.name?.let { append(", name=$it") }
            append(", id=${label.id ?: label.hashCode()}")
            initializer?.let { append(", initializer=$it") }
            location?.let { append(", location=$it") }
            append(")")
        }
    }
}