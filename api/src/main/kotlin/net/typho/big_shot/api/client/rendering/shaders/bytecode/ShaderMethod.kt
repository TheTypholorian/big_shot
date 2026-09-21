package net.typho.big_shot.api.client.rendering.shaders.bytecode

data class ShaderMethod<I : IShaderInsn> @JvmOverloads constructor(
    @JvmField
    val type: ShaderBytecodeType.Function,
    @JvmField
    val label: ShaderLabelNode = ShaderLabelNode()
) {
    @JvmField
    var controlMask = 0
    @JvmField
    val insns = mutableListOf<I>()

    fun call(result: ShaderLabelNode, vararg args: ShaderLabelNode) = ShaderInsnNode(OP_FUNCTION_CALL, type.returnType, result, label, *args)

}