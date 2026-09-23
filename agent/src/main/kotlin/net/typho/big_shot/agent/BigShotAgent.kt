package net.typho.big_shot.agent

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
import net.typho.big_shot.agent.platform.fabric.BigShotFabric
import net.typho.big_shot.agent.transform.RemapEvent
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.agent.transform.impl.BuiltinClassTweakerTransform
import net.typho.big_shot.agent.transform.impl.ButIWantThatInMyMixinPackageTransform
import net.typho.big_shot.agent.transform.impl.ClassLoadingFixTransform
import net.typho.big_shot.agent.transform.impl.InjectMixinTransforms
import net.typho.big_shot.agent.transform.impl.InjectMixinUtilsTransform
import net.typho.big_shot.agent.transform.impl.KotlinMixinFixer
import net.typho.big_shot.agent.transform.impl.RemapTransform
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.nio.file.Files
import java.nio.file.Paths
import java.security.ProtectionDomain
import kotlin.io.path.*

object BigShotAgent : ClassFileTransformer {
    @JvmField
    val DEBUG_PATH = Paths.get(".big_shot_debug")
    @JvmField
    val AGENT_PATH = javaClass.protectionDomain.codeSource.location.toURI().toPath()
    @JvmField
    val API_PATH = Files.createTempDirectory("big_shot_agent").resolve("api.jar")

    @get:JvmName("getInstrumentation")
    lateinit var INSTRUMENTATION: Instrumentation
    @JvmField
    val TRANSFORM_EVENTS = EventGraph<String, TransformEvent>()
    @JvmField
    val REMAP_EVENTS = EventGraph<String, RemapEvent>()

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

    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain,
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
            Log.error("Error transforming class $className\nTransform event graph:\n$TRANSFORM_EVENTS", t)

            return null
        }
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        AgentLoadedCheck.loaded = true
        Log.info("Loading big shot agent from $AGENT_PATH")

        INSTRUMENTATION = inst

        TRANSFORM_EVENTS.register(
            ClassLoadingFixTransform,
            BuiltinClassTweakerTransform,
            ButIWantThatInMyMixinPackageTransform,
            InjectMixinTransforms,
            InjectMixinUtilsTransform,
            KotlinMixinFixer,
            RemapTransform,
            BigShotFabric
        )

        DEBUG_PATH.deleteRecursively()

        API_PATH.outputStream().use { output ->
            javaClass.classLoader.getResourceAsStream("big_shot/api.jar").use { input ->
                input!!.copyTo(output)
            }
        }

        inst.addTransformer(this, true)
    }
}