package net.typho.big_shot.loader

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
import net.typho.big_shot.loader.mixin.jumps.BreakLoop
import net.typho.big_shot.loader.mixin.jumps.BreakLoopSugarApplicator
import net.typho.big_shot.loader.mixin.jumps.Jump
import net.typho.big_shot.loader.mixin.jumps.JumpSugarApplicator
import net.typho.big_shot.loader.mixin.target.SwitchInjectionPoint
import net.typho.big_shot.loader.mixin.target.TypeInjectionPoint
import net.typho.big_shot.loader.util.EventGraph
import net.typho.big_shot.loader.transform.RemapEvent
import net.typho.big_shot.loader.transform.TransformEvent
import net.typho.big_shot.loader.transform.TransformSource
import net.typho.big_shot.loader.mixin.kotlin.KotlinMixinFixer
import net.typho.big_shot.loader.transform.impl.BuiltinClassTweakerTransform
import net.typho.big_shot.loader.transform.impl.ButIWantThatInMyMixinPackageTransform
import net.typho.big_shot.loader.transform.impl.InjectMixinUtilsTransform
import net.typho.big_shot.loader.transform.impl.RemapTransform
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.transformer.ClassInfo
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.nio.file.Path
import java.nio.file.Paths
import java.security.ProtectionDomain
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeBytes
import kotlin.jvm.java

object BigShotLoader {
    @get:JvmName("getInstrumentation")
    lateinit var INSTRUMENTATION: Instrumentation
    @JvmField
    val TRANSFORM_EVENTS = EventGraph<String, TransformEvent>(
        BuiltinClassTweakerTransform,
        ButIWantThatInMyMixinPackageTransform,
        InjectMixinUtilsTransform,
        KotlinMixinFixer,
        RemapTransform
    )
    @JvmField
    val REMAP_EVENTS = EventGraph<String, RemapEvent>()

    @get:JvmName("getLoaderPath")
    lateinit var LOADER_PATH: Path
    @JvmField
    val DEBUG_PATH = Paths.get(".big_shot_debug")

    @JvmStatic
    fun debugSaveClass(
        node: ClassNode
    ) {
        val writer = ClassWriter(0)
        node.accept(writer)
        debugSaveClass(node.name, writer.toByteArray())
    }

    @JvmStatic
    fun debugSaveClass(
        className: String,
        bytes: ByteArray
    ) {
        val path = DEBUG_PATH.resolve("$className.class")
        path.createParentDirectories()
        path.writeBytes(bytes)
    }

    @Suppress("unused")
    @JvmStatic
    fun onInstrumentationInit() {
        INSTRUMENTATION.addTransformer(object : ClassFileTransformer {
            override fun transform(
                loader: ClassLoader,
                className: String,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                bytes: ByteArray
            ): ByteArray? {
                try {
                    val info = ClassTransformInfo.ByteTransform(bytes)

                    TRANSFORM_EVENTS.execute { id, event ->
                        info.fallbackErrorSource = id
                        event.transform(TransformSource.CLASS, info)
                    }

                    return info.compile(::debugSaveClass)
                } catch (t: Throwable) {
                    ClassVisitException("Error transforming class $className\nTransform event graph:\n$TRANSFORM_EVENTS", t).printStackTrace()

                    return null
                }
            }
        }, true)
    }

    @Suppress("unused")
    @JvmStatic
    fun registerExtraMixinSugars(sugars: List<Pair<Class<out Annotation>, Class<out SugarApplicator>>>): List<Pair<Class<out Annotation>, Class<out SugarApplicator>>> {
        return sugars + listOf(
            Pair.of(BreakLoop::class.java, BreakLoopSugarApplicator::class.java),
            Pair.of(Jump::class.java, JumpSugarApplicator::class.java)
        )
    }

    @Suppress("unused", "deprecation", "RedundantSuppression")
    @JvmStatic
    fun registerInjectionPoints() {
        InjectionPoint.register(SwitchInjectionPoint::class.java)
        InjectionPoint.register(TypeInjectionPoint::class.java)
    }

    @Suppress("unused")
    @JvmStatic
    fun bypassMixinPackageRestriction(info: ClassInfo): Boolean = info.name.startsWith("net/typho/big_shot")

    @Suppress("unused")
    @JvmStatic
    fun transformMixinInfo(node: ClassNode) {
        try {
            val info = ClassTransformInfo.Wrapper(node)

            TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transform(TransformSource.MIXIN, info)
            }

            info.checkErrors()

            if (info.changed) {
                debugSaveClass(node)
            }
        } catch (t: Throwable) {
            throw ClassVisitException("Error transforming mixin class ${node.name}\nTransform event graph:\n$TRANSFORM_EVENTS", t)
        }
    }

    @Suppress("unused")
    @JvmStatic
    fun transformClassInfo(node: ClassNode) {
        try {
            val info = ClassTransformInfo.Wrapper(node)

            TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transform(TransformSource.CLASS_INFO, info)
            }

            info.checkErrors()

            if (info.changed) {
                debugSaveClass(node)
            }
        } catch (t: Throwable) {
            throw ClassVisitException("Error transforming mixin class ${node.name}\nTransform event graph:\n$TRANSFORM_EVENTS", t)
        }
    }

    @Suppress("unused")
    @JvmStatic
    fun onLoaderInit() {
        println("yay loader init! $INSTRUMENTATION")
    }
}