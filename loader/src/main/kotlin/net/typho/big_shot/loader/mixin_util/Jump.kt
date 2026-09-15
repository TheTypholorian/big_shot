package net.typho.big_shot.loader.mixin_util

import com.llamalad7.mixinextras.sugar.Local
import org.spongepowered.asm.mixin.injection.At

/**
 * A mixin sugar annotation that allows for an arbitrary jump to any place in the target method.
 *
 * Arguments must be of type [JumpHandle].
 *
 * Example for usage:
 * ```java
 * public static void targetMethod() {
 *     if (...) {
 *         methodA();
 *     } else {
 *         methodB();
 *     }
 * }
 *
 * @Inject(
 *     method = "targetMethod",
 *     at = @At(
 *         value = "INVOKE",
 *         target = "methodB"
 *     )
 * )
 * private static void mixinMethod(
 *     CallbackInfo ci,
 *     @Jump(
 *         value = @At(
 *             value = "INVOKE",
 *             target = "methodA"
 *         )
 *     ) JumpHandle jump
 * ) {
 *     if (shouldJump) {
 *         jump.jump();
 *     }
 * }
 * ```
 * The mixin'd output will look like this:
 * ```java
 * public static void targetMethod() {
 *     if (...) {
 *         methodA();
 *     } else {
 *         JumpHandle.Impl jumpHandle0 = new JumpHandle.Impl(0, 0);
 *         mixinMethod(null, jumpHandle0);
 *
 *         if (jumpHandle0) {
 *             methodA();
 *         } else {
 *             methodB();
 *         }
 *     }
 * }
 * ```
 * though that is not exact, and there will only be **one** `methodA` call in the bytecode.
 */
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
     * Number of locals (highest first) to modify at the jump target.
     *
     * For example, take this code:
     * ```java
     * if (...) {
     *     int i = 10;
     *     System.out.println(i);
     * }
     *
     * someMethod();
     * ```
     * If you jump from the `someMethod` call to the `println` call, the `i` local variable will be uninitialized, which is problematic.
     * There are two ways to deal with this.
     * Option 1 is to instead jump before `i` is initialized, and it will be set to 10 (or whatever value is mixin'd in).
     *
     * However, if you want `i` to be a different value (or other local variables), then use `localsToModify`.
     * Specify each local you want to modify, then use `JumpHandle.setLocal` to modify them (`setLocal` calls will be ignored unless the jump is invoked).
     * Note that the indices for `setLocal` are indices into the `localsToModify` array.
     *
     * Going back to the original example, your mixin would look like this for option 2:
     * ```java
     * @Inject(
     *     method = "...",
     *     at = @At(
     *         value = "INVOKE",
     *         target = "someMethod"
     *     )
     * )
     * private void example(
     *     CallbackInfo ci,
     *     @Jump(
     *         value = @At(
     *             value = "INVOKE",
     *             target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"
     *         ),
     *         shiftBeforeStack = true,
     *         localsToModify = @Local(type = int.class)
     *     ) JumpHandle jump
     * ) {
     *     if (shouldJump) {
     *         jump.jump();
     *         jump.setLocal(0, <value for i>);
     *     }
     * }
     * ```
     *
     * For compatibility, in the case that your code could have `i` be the default or be a different value, it is good design to use two separate jumps (one with option 1 and another with option 2).
     */
    val localsToModify: Array<Local> = [],
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
     * jumpHandle.setStack(1, System.out);
     * jumpHandle.setStack(0, "abc");
     * ```
     * which would ignore other mixins changing those values. The index parameter for `setStack` is highest first.
     *
     * Note that the above code can be used to combine a jump and a `@ModifyArgs`, which might be useful in some cases.
     */
    val shiftBeforeStack: Boolean = false
)
