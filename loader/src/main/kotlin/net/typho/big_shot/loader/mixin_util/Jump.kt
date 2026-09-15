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
    val value: At,
    /**
     * If true, shifts the jump target back until the stack is empty.
     *
     * For example, take this code:
     * ```java
     * System.out.println("abc");
     * ```
     * Targeting the `println` would normally look like this:
     * ```java
     * PrintStream var0 = System.out;
     * String var1 = "abc";
     * // injection here
     * var0.println(var1);
     * ```
     * However, if `shiftBeforeStack` is true, it then looks like this:
     * ```java
     * // injection here
     * System.out.println("abc");
     * ```
     * If you had `shiftBeforeStack` set to false, then you would need to restate the stack values for `System.out` and `"abc"` in your mixin, like this:
     * ```java
     * jumpHandle.jump();
     * jumpHandle.setStack(0, System.out);
     * jumpHandle.setStack(1, "abc");
     * ```
     * which would ignore other mixins changing those values.
     *
     * Note that the above code can be used to combine a jump and a `@ModifyArgs`, which might be useful in some cases.
     */
    val shiftBeforeStack: Boolean = false
)
