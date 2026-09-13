package net.typho.big_shot.loader

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import net.typho.asm_util.ASMUtil
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.asm_util.remap.CompatClassRemapper
import net.typho.big_shot.loader.constant.TransformEventNames
import net.typho.big_shot.loader.mixin_util.jump.BreakLoop
import net.typho.big_shot.loader.mixin_util.jump.BreakLoopSugarApplicator
import net.typho.big_shot.loader.mixin_util.jump.Jump
import net.typho.big_shot.loader.mixin_util.jump.JumpSugarApplicator
import net.typho.big_shot.loader.mixin_util.switches.AddSwitchBranchInjectionInfo
import net.typho.big_shot.loader.mixin_util.switches.SwitchInjectionPoint
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
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
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
        TRANSFORM_EVENTS.register(TransformEventNames.ACCESS_WIDENERS) { type, info, className ->
            when (className) {
                "com/llamalad7/mixinextras/sugar/impl/SugarApplicator" -> {
                    info.node.access = ASMUtil.accessPublic(info.node.access)
                    info.node.fields.forEach { it.access = ASMUtil.accessPublic(it.access) }
                    info.node.methods.forEach { it.access = ASMUtil.accessPublic(it.access) }
                    info.markChanged()
                }
                "com/llamalad7/mixinextras/sugar/impl/SugarPostProcessingExtension" -> {
                    MethodPointer.method().name("enqueuePostProcessing").find(info.node).forEach { it.access = ASMUtil.accessPublic(it.access) }
                    info.markChanged()
                }
            }
        }
        TRANSFORM_EVENTS.register(TransformEventNames.MIXIN_UTILS) { type, info, className ->
            when (className) {
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
                "org/spongepowered/asm/mixin/injection/struct/InjectionInfo" -> {
                    MethodPointer.method().name("<clinit>").findOrThrow(info.node) { method ->
                        InsnPointer.simple().opcode(Opcodes.RETURN).findOrThrow(method.instructions) { insn ->
                            method.instructions.insertBefore(insn, MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "net/typho/big_shot/loader/BigShotLoader",
                                "registerInjectionInfos",
                                "()V"
                            ))
                        }
                    }
                    info.markChanged()
                    info.computeFrames()
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
        }.after(TransformEventNames.ACCESS_WIDENERS)
        TRANSFORM_EVENTS.register(TransformEventNames.KOTLIN_MIXIN_FIXER) { type, info, className ->
            if (type == TransformType.MIXIN) {
                if (KotlinMixinFixer.fix(info.node)) {
                    info.markChanged()
                }
            }
        }
        TRANSFORM_EVENTS.register(TransformEventNames.REMAP) { type, info, className ->
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
                        event.transform(TransformType.CLASS, info, className)
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

    @Suppress("unused")
    @JvmStatic
    fun registerInjectionInfos() {
        InjectionInfo.register(AddSwitchBranchInjectionInfo::class.java)
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
                event.transform(TransformType.MIXIN, info, node.name)
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