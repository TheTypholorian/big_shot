package net.typho.big_shot.loader.client.rendering.shaders.bytecode

data class ShaderVariable @JvmOverloads constructor(
    @JvmField
    val type: ShaderBytecodeType.Pointer,
    @JvmField
    val label: ShaderLabelNode = ShaderLabelNode(),
    @JvmField
    val initializer: ShaderLabelNode? = null,
    @JvmField
    val location: Int? = null
)