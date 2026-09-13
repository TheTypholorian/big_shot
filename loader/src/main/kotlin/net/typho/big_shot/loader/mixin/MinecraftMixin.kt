package net.typho.big_shot.loader.mixin

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import net.minecraft.client.Minecraft
import net.typho.big_shot.loader.mixin_util.jump.BreakLoop
import net.typho.big_shot.loader.mixin_util.jump.JumpHandle
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Debug(export = true)
@Mixin(Minecraft::class)
class MinecraftMixin {
    @WrapOperation(
        method = ["createTitle"],
        at = [At(
            value = "INVOKE",
            target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"
        )]
    )
    private fun createTitle(instance: StringBuilder, operation: Operation<String>): String {
        return operation.call(instance.append(" + Big Shot Loader"))
    }

    @Inject(
        method = ["run"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/SingleTickProfiler;createTickProfiler(Ljava/lang/String;)Lnet/minecraft/util/profiling/SingleTickProfiler;"
        )]
    )
    private fun run(ci: CallbackInfo, @BreakLoop breakLoop: JumpHandle) {
        if (1 > 2) {
            breakLoop.jump()
        }
    }
}