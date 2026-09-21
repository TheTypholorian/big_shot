package net.typho.big_shot.api.client.rendering.ssbo

import com.mojang.blaze3d.opengl.Uniform

data class SsboUniform(
    @JvmField
    val binding: Int
) : Uniform
