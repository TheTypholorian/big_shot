package net.typho.big_shot.api.mixin.client

import net.minecraft.ChatFormatting
import net.minecraft.client.resources.SplashManager
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(SplashManager::class)
class SplashManagerMixin {
    @Shadow
    private lateinit var splashes: List<Component>

    @Inject(
        method = ["apply"],
        at = [At("TAIL")]
    )
    private fun apply(ci: CallbackInfo) {
        splashes += Component.literal("Now's your chance to be a ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("[[").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("BIG ").withStyle(Style.EMPTY.withColor(0xFFAEC9)))
            .append(Component.literal("SHOT").withStyle(Style.EMPTY.withColor(0xFFF301)))
            .append(Component.literal("]]").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("!!!").withStyle(ChatFormatting.GRAY))
    }
}