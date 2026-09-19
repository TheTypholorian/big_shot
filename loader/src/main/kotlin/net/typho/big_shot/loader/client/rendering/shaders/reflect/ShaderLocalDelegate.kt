package net.typho.big_shot.loader.client.rendering.shaders.reflect

import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderBytecodeType
import net.typho.big_shot.loader.client.rendering.shaders.bytecode.ShaderLabelNode

interface ShaderLocalDelegate {
    fun createLocalVariable(type: ShaderBytecodeType, label: ShaderLabelNode): ShaderLocal.Variable
}