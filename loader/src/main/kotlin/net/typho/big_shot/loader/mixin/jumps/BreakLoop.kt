package net.typho.big_shot.loader.mixin.jumps

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class BreakLoop(
    /**
     * The depth of the loop, from 0 (set to -1 to pick the smallest loop).
     */
    val depth: Int = -1
)
