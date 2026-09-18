package net.typho.big_shot.loader.mixin.kotlin

import org.spongepowered.asm.mixin.Shadow

/**
 * [org.spongepowered.asm.mixin.Shadow] but for Kotlin abstract properties.
 *
 * ```java
 * @Shadow
 * @Final
 * private Map<String, Integer> field;
 * ```
 * converts to
 * ```kotlin
 * @KtShadow
 * protected abstract val field: MutableMap<String, Int>
 * ```
 *
 * The @Final is added automatically if needed, and the access is lowered from protected to private.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class KtShadow(
    val value: Shadow = Shadow()
)