package net.typho.big_shot.loader.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.typho.big_shot.loader.TestMixinTarget;
import net.typho.big_shot.loader.mixin_util.Jump;
import net.typho.big_shot.loader.mixin_util.JumpHandle;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintStream;

@Debug(export = true)
@Mixin(TestMixinTarget.class)
public class TestMixin {
    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(
            method = "main",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void yay(CallbackInfo ci) {
    }

    @WrapOperation(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/PrintStream;println(F)V"
            )
    )
    private static void mineNowNyeheheh(PrintStream instance, float f, Operation<Void> original) {
        original.call(instance, f + 5);
    }

    @ModifyConstant(
            method = "main",
            constant = @Constant(intValue = 10)
    )
    private static int butWhatIf(int i) {
        return i * 2;
    }

    @Inject(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"
            ),
            cancellable = true
    )
    private static void main(
            CallbackInfo ci,
            @Local LocalFloatRef f,
            @Jump(
                    value = @At(
                            value = "INVOKE",
                            target = "Ljava/io/PrintStream;println(F)V"
                    ),
                    shiftBeforeStack = true,
                    localsToModify = @Local(type = int.class)
            ) JumpHandle.Complex jump1,
            @Jump(
                    value = @At(
                            value = "INVOKE",
                            target = "Ljava/io/PrintStream;println(F)V"
                    ),
                    localsToModify = @Local(type = int.class)
            ) JumpHandle.Complex jump2
    ) {
        if (Math.random() > 0.999) {
            ci.cancel();
        }

        if (Math.random() > 0.5) {
            jump1.jump();
            jump1.setLocal(0, 15);
            f.set(20);
        } else if (Math.random() > 0.5) {
            jump2.jump();
            jump2.setStack(0, System.out);
            jump2.setStack(1, 10f);
            jump2.setLocal(0, 10);
        }
    }
}
