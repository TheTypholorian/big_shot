package net.typho.big_shot.loader.client.rendering.shaders.bytecode

import net.typho.big_shot.loader.util.ExpandingByteBuffer

interface IShaderInsn {
    fun write(builder: ShaderBytecodeBuilder, buffer: ExpandingByteBuffer)
}