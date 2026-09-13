package net.typho.big_shot.loader.mixin;

import net.typho.big_shot.loader.TestMixinTarget;
import net.typho.big_shot.loader.mixin_util.switches.AddSwitchBranch;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(TestMixinTarget.class)
public class TestMixin {
    @AddSwitchBranch(
            method = {"switch0", "switchX", "switch2", "main"},
            at = @At(
                    value = "BIG_SHOT:SWITCH",
                    ordinal = 0
            ),
            intValue = -1
    )
    private static void switch0Inject() {
        System.out.println("d");
    }

    @AddSwitchBranch(
            method = {"switch3", "switchY"},
            at = @At(
                    value = "BIG_SHOT:SWITCH",
                    ordinal = 0
            ),
            intValue = -1
    )
    private static String switch3Inject() {
        return "w";
    }

    /*
    @WrapOperation(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/PrintStream;println(I)V"
            )
    )
    private static void main1(PrintStream instance, int i, Operation<Void> original) {
        original.call(instance, i);
    }

    @Inject(
            method = "main",
            at = @At(
                    value = "FIELD",
                    target = "Ljava/lang/System;out:Ljava/io/PrintStream;",
                    ordinal = 3,
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static void main2(CallbackInfo ci, @Local int i, @Jump(@At("TAIL")) JumpHandle jumpHandle1, @Jump(@At(
            value = "INVOKE",
            target = "Ljava/io/PrintStream;println(I)V"
    )) JumpHandle jumpHandle2) {
        if (Math.random() > 0.1 && i == 10) {
            jumpHandle2.jump();
        } else {
            jumpHandle1.jump();
        }
    }
     */
}
