package net.typho.big_shot.loader

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.asm_util.remap.CompatClassRemapper
import net.typho.big_shot.data.ct.BuiltinClassTweaker
import net.typho.big_shot.loader.constant.TransformEventNames
import net.typho.big_shot.loader.mixin_util.BreakLoop
import net.typho.big_shot.loader.mixin_util.BreakLoopSugarApplicator
import net.typho.big_shot.loader.mixin_util.Jump
import net.typho.big_shot.loader.mixin_util.JumpSugarApplicator
import net.typho.big_shot.loader.mixin_util.SwitchInjectionPoint
import net.typho.big_shot.loader.util.EventGraph
import net.typho.big_shot.loader.util.inst.RemapEvent
import net.typho.big_shot.loader.util.inst.TransformEvent
import net.typho.big_shot.loader.util.inst.TransformType
import net.typho.big_shot.loader.util.mixin.KotlinMixinFixer
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.spongepowered.asm.mixin.injection.InjectionPoint
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
    val TRANSFORM_EVENTS = EventGraph<String, TransformEvent>()
    @JvmField
    val REMAP_EVENTS = EventGraph<String, RemapEvent>()

    @get:JvmName("getLoaderPath")
    lateinit var LOADER_PATH: Path
    @JvmField
    val DEBUG_PATH = Paths.get(".big_shot_debug")

    init {
        TRANSFORM_EVENTS.register(TransformEventNames.EARLY_ACCESS_WIDENER) { type, info ->
            BuiltinClassTweaker.apply(info)
        }
        TRANSFORM_EVENTS.register(TransformEventNames.MIXIN_UTILS) { type, info ->
            when (info.className) {
                "com/llamalad7/mixinextras/sugar/impl/SugarApplicator" -> {
                    MethodPointer.method().name("<clinit>").findOrThrow(info.node) { method ->
                        InsnPointer.methodCallStatic().owner("java/util/Arrays").name("asList").ordinal(0).findOrThrow(method.instructions) { insn ->
                            method.instructions.insert(insn, MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "net/typho/big_shot/loader/BigShotLoader",
                                "registerExtraMixinSugars",
                                "(Ljava/util/List;)Ljava/util/List;"
                            ))
                        }
                    }
                    info.markChanged()
                }
                "org/spongepowered/asm/mixin/injection/InjectionPoint" -> {
                    MethodPointer.method().name("<clinit>").findOrThrow(info.node) { method ->
                        InsnPointer.simple().opcode(Opcodes.RETURN).findOrThrow(method.instructions) { insn ->
                            method.instructions.insertBefore(insn, MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "net/typho/big_shot/loader/BigShotLoader",
                                "registerInjectionPoints",
                                "()V"
                            ))
                        }
                    }
                    info.markChanged()
                    info.computeFrames()
                }
            }
        }.after(TransformEventNames.EARLY_ACCESS_WIDENER)
        TRANSFORM_EVENTS.register(TransformEventNames.KOTLIN_MIXIN_FIXER) { type, info ->
            if (type == TransformType.MIXIN) {
                if (KotlinMixinFixer.fix(info.node)) {
                    info.markChanged()
                }
            }
        }
        TRANSFORM_EVENTS.register(TransformEventNames.REMAP) { type, info ->
            val newNode = ClassNode()
            val visitor = REMAP_EVENTS.resolve().foldRight(newNode as ClassVisitor) { event, visitor ->
                val remapper = event.event.createRemapper(info)
                if (remapper == null) visitor else CompatClassRemapper(visitor, remapper)
            }

            if (visitor !== newNode) {
                info.node.accept(visitor)
                info.node = newNode
            }
        }.after(TransformEventNames.KOTLIN_MIXIN_FIXER) // we want to remap after kotlin mixins are fixed, since companion objects
    }

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
                    val info = ClassTransformInfo.AgentTransform(bytes)

                    TRANSFORM_EVENTS.execute { id, event ->
                        info.fallbackErrorSource = id
                        event.transform(TransformType.CLASS, info)
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
    }

    @Suppress("unused")
    @JvmStatic
    fun transformMixinClass(node: ClassNode) {
        try {
            val info = ClassTransformInfo.Wrapper(node)

            TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transform(TransformType.MIXIN, info)
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