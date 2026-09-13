package net.typho.big_shot.loader.mixin_util.switches

import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Desc
import org.spongepowered.asm.mixin.injection.Slice

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AddSwitchBranch(
    val method: Array<String> = [],
    val target: Array<Desc> = [],
    val slice: Array<Slice> = [],
    val at: Array<At> = [],
    val intValue: Int = 0,
    val enumValue: String = "",
    val remap: Boolean = false
)
