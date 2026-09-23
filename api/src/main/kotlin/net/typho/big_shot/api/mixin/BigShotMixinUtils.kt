package net.typho.big_shot.api.mixin

import com.llamalad7.mixinextras.service.MixinExtrasService
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import net.typho.big_shot.api.mixin.jumps.BreakLoop
import net.typho.big_shot.api.mixin.jumps.BreakLoopSugarApplicator
import net.typho.big_shot.api.mixin.jumps.Jump
import net.typho.big_shot.api.mixin.jumps.JumpSugarApplicator
import net.typho.big_shot.api.mixin.target.SwitchInjectionPoint
import net.typho.big_shot.api.mixin.target.TypeInjectionPoint
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.asm.mixin.injection.InjectionPoint

@Suppress("unused")
object BigShotMixinUtils {
    @JvmStatic
    fun registerSugar(anno: Class<out Annotation>, applicator: Class<out SugarApplicator>) {
        for (name in MixinExtrasService.getInstance().getAllClassNames(anno.name)) {
            SugarApplicator.MAP["L${name.replace('.', '/')};"] = applicator
        }
    }

    @ApiStatus.Internal
    @JvmStatic
    fun register() {
        println("Registering big shot mixin utils")

        registerSugar(BreakLoop::class.java, BreakLoopSugarApplicator::class.java)
        registerSugar(Jump::class.java, JumpSugarApplicator::class.java)

        InjectionPoint.register(SwitchInjectionPoint::class.java)
        InjectionPoint.register(TypeInjectionPoint::class.java)
    }
}