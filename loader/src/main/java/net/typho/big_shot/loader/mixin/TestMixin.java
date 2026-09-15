package net.typho.big_shot.loader.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.typho.big_shot.loader.TestMixinTarget;
import net.typho.big_shot.loader.mixin_util.Jump;
import net.typho.big_shot.loader.mixin_util.JumpHandle;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(TestMixinTarget.class)
public class TestMixin {
    @Inject(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"
            )
    )
    private static void main(CallbackInfo ci, @Jump(value = @At(
            value = "INVOKE",
            target = "Ljava/io/PrintStream;println(I)V"
    ), shiftBeforeStack = true, localsToModify = @Local(type = int.class)) JumpHandle.Complex jump) {
        jump.jump();
        jump.setLocal(0, 15);
        //jump.setStack(0, System.out);
        //jump.setStack(1, (int) 'D');
    }
}
