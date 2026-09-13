package net.typho.big_shot.loader.mixin;

import net.typho.big_shot.loader.TestMixinTarget;
import net.typho.big_shot.loader.mixin_util.jump.Jump;
import net.typho.big_shot.loader.mixin_util.jump.JumpHandle;
import org.objectweb.asm.Opcodes;
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
                    value = "FIELD",
                    target = "Ljava/lang/System;out:Ljava/io/PrintStream;",
                    ordinal = 3,
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static void main1(CallbackInfo ci, @Jump(@At(
            value = "FIELD",
            target = "Ljava/lang/System;out:Ljava/io/PrintStream;",
            ordinal = 0,
            opcode = Opcodes.GETSTATIC
    )) JumpHandle jumpHandle2) {
        if (Math.random() > 0.1) {
            jumpHandle2.jump();
        }
    }
}
