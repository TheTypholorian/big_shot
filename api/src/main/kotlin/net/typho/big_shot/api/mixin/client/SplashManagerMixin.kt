package net.typho.big_shot.api.mixin.client

import net.minecraft.ChatFormatting
import net.minecraft.client.resources.SplashManager
import net.minecraft.network.chat.Component
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.awt.Color

@Mixin(SplashManager::class)
class SplashManagerMixin {
    @Shadow
    private lateinit var splashes: List<Component>

    @Inject(
        method = ["apply"],
        at = [At("TAIL")]
    )
    private fun apply(ci: CallbackInfo) {
        var text = Component.literal("Now's your chance to be a ") * ChatFormatting.GRAY
        text += Component.literal("[[") * ChatFormatting.WHITE
        text += Component.literal("BIG ") * Color(0xFFAEC9)
        text += Component.literal("SHOT") * Color(0xFFF301)
        text += Component.literal("]]") * ChatFormatting.WHITE
        text += Component.literal("!!!") * ChatFormatting.GRAY
        splashes += text
    }
}