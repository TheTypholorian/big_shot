package net.typho.big_shot.loader.mixin.client.rendering.ssbo

import com.mojang.blaze3d.shaders.UniformType
import org.spongepowered.asm.mixin.Mixin

@Mixin(UniformType::class)
enum class UniformTypeMixin {
    BIG_SHOT_SSBO
}