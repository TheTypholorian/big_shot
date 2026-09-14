package net.typho.big_shot.loader.mixin_util

import org.spongepowered.asm.mixin.injection.At

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class Jump(
    /**
     * The instruction to jump to.
     *
     * **WARNING**: The target gets shifted back until the stack is empty.
     */
    val value: At
)
