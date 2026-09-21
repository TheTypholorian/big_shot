package net.typho.big_shot.loader.client.rendering.shaders.bytecode

import net.typho.big_shot.loader.util.ExpandingByteBuffer

interface IShaderInsn {
    fun write(builder: ShaderBytecodeBuilder, buffer: ExpandingByteBuffer)

    companion object {
        @JvmStatic
        fun multi(vararg insns: IShaderInsn?) = object : IShaderInsn {
            override fun write(
                builder: ShaderBytecodeBuilder,
                buffer: ExpandingByteBuffer
            ) {
                insns.forEach { it?.write(builder, buffer) }
            }
        }
    }
}