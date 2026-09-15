package net.typho.big_shot.loader.mixin;

import net.typho.big_shot.loader.TestMixinTarget;
import net.typho.big_shot.loader.mixin_util.Jump;
import net.typho.big_shot.loader.mixin_util.JumpHandle;
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
                    opcode = Opcodes.GETSTATIC,
                    ordinal = 0
            )
    )
    private static void main(CallbackInfo ci, @Jump(@At(
            value = "FIELD",
            target = "Ljava/lang/System;out:Ljava/io/PrintStream;",
            opcode = Opcodes.GETSTATIC,
            ordinal = 2
    )) JumpHandle jump) {
        jump.jump();
        //jump.setStack(0, System.out);
        //jump.setStack(1, "w");
    }
}
