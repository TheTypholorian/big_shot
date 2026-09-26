package net.typho.big_shot.agent.platform.neoforge

import net.fabricmc.loader.impl.util.LoaderUtil
import net.fabricmc.loader.impl.util.UrlUtil
import net.neoforged.fml.ModList
import net.neoforged.fml.ModLoader
import net.neoforged.fml.loading.FMLLoader
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.platform.BigShotPlatform
import net.typho.big_shot.agent.platform.fabric.FabricPlatform
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.jetbrains.annotations.ApiStatus
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.net.URL
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

@ApiStatus.Internal
@Suppress("unused")
object NeoForgePlatform : BigShotPlatform, EventGraph.SelfAware<String>, TransformEvent {
    override val allMods: List<PlatformMod>
        get() = ModList.get().sortedMods.map { NeoForgeModImpl(it) }
    override val id: String
        get() = "big_shot:platform/neoforge"
    override var loaded = false
        private set
    override val classLoader: ClassLoader
        get() = FMLLoader.getCurrent().currentClassLoader ?: Thread.currentThread().contextClassLoader

    init {
        LOG = NeoForgeLogImpl
        LOG.info("Loading big shot on neoforge")
        BigShotAgent.TRANSFORM_EVENTS.register(this)
    }

    override fun getModAt(path: Path): PlatformMod? {
        if (!loaded) {
            return null
        }

        return ModList.get().sortedMods.firstOrNull { it.modInfo.owningFile.file.filePath == path }?.let { NeoForgeModImpl(it) }
    }

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "net/neoforged/fml/classloading/transformation/TransformingClassLoader" -> {
                info.markChanged()

                MethodPointer.method()
                    .name("maybeTransformClassBytes")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insertBefore(
                            InsnPointer.simple()
                                .opcode(Opcodes.ARETURN)
                                .lastOrdinal()
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(VarInsnNode(Opcodes.ALOAD, 2))
                                add(MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "net/typho/big_shot/agent/platform/neoforge/NeoForgePlatform",
                                    "getRawClassByteArray",
                                    "([BLjava/lang/String;)[B"
                                ))
                            }
                        )
                    }
            }
        }
    }

    @JvmStatic
    fun getRawClassByteArray(bytes: ByteArray, className: String): ByteArray {
        val binaryName = className.replace('.', '/')

        try {
            var mod: PlatformMod? = null

            try {
                // TODO this feels pretty jank
                val list = FMLLoader.getCurrent().loadingModList
                mod = list.packageIndex[className.substringBeforeLast('.')]?.modInfos?.firstOrNull()?.modId?.let { ModList.get()?.getModContainerById(it)?.map { NeoForgeModImpl(it) }?.getOrNull() }
            } catch (t: Throwable) {
                LOG.error("Error finding owner mod for class $binaryName", t)
            }

            //LOG.info("Transforming class $binaryName $mod")

            val info = ClassTransformInfo.ByteTransform(bytes, binaryName)

            BigShotAgent.TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transform(mod, info)
            }

            return info.compile(BigShotAgent::debugSaveClass) ?: bytes
        } catch (t: Throwable) {
            LOG.error("Error transforming class $binaryName\nTransform event graph:\n${BigShotAgent.TRANSFORM_EVENTS}", t)

            return bytes
        }
    }
}