package net.typho.big_shot.api.mixin

import net.minecraft.network.chat.MutableComponent
import net.typho.big_shot.api.util.MutableComponentExtension
import org.spongepowered.asm.mixin.Mixin

@Mixin(MutableComponent::class)
class MutableComponentMixin : MutableComponentExtension