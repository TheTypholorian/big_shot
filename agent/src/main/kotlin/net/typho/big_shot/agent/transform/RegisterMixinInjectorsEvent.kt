package net.typho.big_shot.agent.transform

import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import net.typho.big_shot.util.event.EventGraph
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.jvm.java

// TODO an event system sucks for this
interface RegisterMixinInjectorsEvent {
    fun registerSugars(out: BiConsumer<Class<out Annotation>, Class<out SugarApplicator>>)

    fun registerInjectors(out: Consumer<Class<out InjectionInfo>>)

    fun registerInjectionPoints(out: Consumer<Class<out InjectionPoint>>)

    object Builtin : EventGraph.SelfAware<String>, RegisterMixinInjectorsEvent {
        override val id: String
            get() = "big_shot:builtin"

        override fun registerSugars(out: BiConsumer<Class<out Annotation>, Class<out SugarApplicator>>) {
            out.accept(BreakLoop::class.java, BreakLoopSugarApplicator::class.java)
            out.accept(Jump::class.java, JumpSugarApplicator::class.java)
        }

        override fun registerInjectors(out: Consumer<Class<out InjectionInfo>>) {
        }

        override fun registerInjectionPoints(out: Consumer<Class<out InjectionPoint>>) {
            out.accept(SwitchInjectionPoint::class.java)
            out.accept(TypeInjectionPoint::class.java)
        }
    }
}