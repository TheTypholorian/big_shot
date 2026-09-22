package net.typho.big_shot.agent.transform

/**
 * Describes what source is requesting the class that is being transformed through [net.typho.big_shot.agent.BigShotAgent.TRANSFORM_EVENTS]
 */
enum class TransformSource(
    @JvmField
    val isMixinClass: Boolean = false,
    @JvmField
    val isFromMixin: Boolean = false,
    @JvmField
    val isRegularClass: Boolean = false
) {
    /**
     * Mixin class requested by [org.spongepowered.asm.mixin.transformer.MixinInfo]
     */
    MIXIN(isMixinClass = true, isFromMixin = true),
    /**
     * Class requested by [org.spongepowered.asm.mixin.transformer.ClassInfo]
     */
    CLASS_INFO(isFromMixin = true, isRegularClass = true),
    /**
     * Regular loaded class
     */
    CLASS(isRegularClass = true)
}