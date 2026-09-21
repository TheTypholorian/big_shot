package net.typho.big_shot.api.mixin.client.rendering.ssbo

import com.mojang.blaze3d.shaders.UniformType
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin

@Debug(export = true)
@Mixin(UniformType::class)
enum class UniformTypeMixin {
    BIG_SHOT_SSBO
}