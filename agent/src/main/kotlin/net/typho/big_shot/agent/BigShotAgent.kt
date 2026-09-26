package net.typho.big_shot.agent

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.platform.BigShotPlatform
import net.typho.big_shot.agent.platform.fabric.FabricPlatform
import net.typho.big_shot.agent.platform.neoforge.NeoForgePlatform
import net.typho.big_shot.agent.transform.RemapEvent
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.impl.*
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.nio.file.Paths
import java.security.ProtectionDomain
import kotlin.io.path.*

object BigShotAgent : ClassFileTransformer {
    @JvmField
    val DEBUG_PATH = Paths.get(".big_shot_debug")
    @JvmField
    val AGENT_PATH = javaClass.protectionDomain.codeSource.location.toURI().toPath()
    @JvmField
    var API_PATH = createTempDirectory("big_shot").resolve("api.jar")

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
        if (className == "net/fabricmc/loader/impl/launch/knot/Knot") {
            BigShotPlatform.INSTANCE = FabricPlatform
        } else if (className == "net/neoforged/fml/startup/Entrypoint") {
            BigShotPlatform.INSTANCE = NeoForgePlatform
        }

        try {
            var mod: PlatformMod? = null

            if (BigShotPlatform.INSTANCE?.loaded == true) {
                try {
                    protectionDomain.codeSource?.location?.toURI()?.toPath()?.let { mod = BigShotPlatform.INSTANCE?.getModAt(it) }
                } catch (t: Throwable) {
                    LOG.error("Error finding owner mod for class $className", t)
                }
            }

            val info = ClassTransformInfo.ByteTransform(bytes)

            TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transform(mod, info)
            }

            return info.compile(::debugSaveClass)
        } catch (t: Throwable) {
            LOG.error("Error transforming class $className\nTransform event graph:\n$TRANSFORM_EVENTS", t)

            return null
        }
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        LOG.info("Loading big shot agent from $AGENT_PATH")
        LOG.info("Writing api jar to $API_PATH")
        javaClass.classLoader.getResourceAsStream("big_shot/api.jar")!!.use { input ->
            API_PATH.outputStream().use { output ->
                input.transferTo(output)
            }
        }

        INSTRUMENTATION = inst

        TRANSFORM_EVENTS.register(
            ClassTweakerTransform,
            InjectMixinTransforms,
            KotlinMixinFixer,
            RemapTransform
        )

        DEBUG_PATH.deleteRecursively()

        inst.addTransformer(this, true)
    }
}