package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.discovery.ModCandidateFinder
import net.fabricmc.loader.impl.discovery.ModCandidateImpl
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.LoaderUtil
import net.fabricmc.loader.impl.util.UrlUtil
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.LOG
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.platform.BigShotPlatform
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.jetbrains.annotations.ApiStatus
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.net.URL
import java.nio.file.Path

@ApiStatus.Internal
@Suppress("unused")
object FabricPlatform : BigShotPlatform, EventGraph.SelfAware<String>, TransformEvent {
    override val allMods: List<PlatformMod>
        get() = FabricLoader.getInstance().allMods.map { FabricModImpl(it) }
    override val id: String
        get() = "big_shot:platform/fabric"
    override var loaded = false
        private set
    override val classLoader: ClassLoader
        get() = FabricLauncherBase.getLauncher().targetClassLoader ?: Thread.currentThread().contextClassLoader

    init {
        LOG = FabricLogImpl
        LOG.info("Loading big shot on fabric")
        BigShotAgent.TRANSFORM_EVENTS.register(this)
    }

    override fun getModAt(path: Path): PlatformMod? {
        if (!loaded) {
            return null
        }

        return FabricLoader.getInstance().allMods.firstOrNull { (it as? ModContainerImpl)?.codeSourcePaths?.contains(path) == true }?.let { FabricModImpl(it) }
    }

    override fun transform(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "net/fabricmc/loader/impl/launch/knot/KnotClassDelegate" -> {
                info.markChanged()
                info.computeFrames()

                // Transforms (these apply to all classes plus mixins)
                MethodPointer.method()
                    .name("getRawClassByteArray")
                    .desc("(Ljava/lang/String;Z)[B")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insertBefore(
                            InsnPointer.simple()
                                .opcode(Opcodes.ARETURN)
                                .lastOrdinal()
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(VarInsnNode(Opcodes.ALOAD, 3))
                                add(VarInsnNode(Opcodes.ALOAD, 1))
                                add(MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "net/typho/big_shot/agent/platform/fabric/FabricPlatform",
                                    "getRawClassByteArray",
                                    "([BLjava/net/URL;Ljava/lang/String;)[B"
                                ))
                            }
                        )
                    }
                // Allow big shot agent classes to be used without knot freaking out (since the agent classes are loaded by java, not knot)
                MethodPointer.method()
                    .name("isValidParentUrl")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(InsnList().apply {
                            val label = LabelNode()
                            add(VarInsnNode(Opcodes.ALOAD, 1))
                            add(VarInsnNode(Opcodes.ALOAD, 2))
                            add(
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "net/typho/big_shot/agent/platform/fabric/FabricPlatform",
                                    "testParentURL",
                                    "(Ljava/net/URL;Ljava/lang/String;)Z"
                                )
                            )
                            add(JumpInsnNode(Opcodes.IFEQ, label))
                            add(InsnNode(Opcodes.ICONST_1))
                            add(InsnNode(Opcodes.IRETURN))
                            add(label)
                        })
                }
            }

            "net/fabricmc/loader/impl/FabricLoaderImpl" -> {
                info.markChanged()
                info.computeMaxStacks()

                // Register the api as a mod, regardless if it's in the mods folder or not
                MethodPointer.method()
                    .name("setup")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(
                            InsnPointer.methodCall()
                                .owner("net/fabricmc/loader/impl/discovery/ModDiscoverer")
                                .name("addCandidateFinder")
                                .desc("(Lnet/fabricmc/loader/impl/discovery/ModCandidateFinder;)V")
                                .ordinal(2)
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(VarInsnNode(Opcodes.ALOAD, 4))
                                add(FieldInsnNode(
                                    Opcodes.GETSTATIC,
                                    $$"net/typho/big_shot/agent/platform/fabric/FabricPlatform$CandidateFinder",
                                    "INSTANCE",
                                    $$"Lnet/typho/big_shot/agent/platform/fabric/FabricPlatform$CandidateFinder;"
                                ))
                                add(MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    "net/fabricmc/loader/impl/discovery/ModDiscoverer",
                                    "addCandidateFinder",
                                    "(Lnet/fabricmc/loader/impl/discovery/ModCandidateFinder;)V"
                                ))
                            }
                        )
                    }

                // Load big shot mod metadata
                MethodPointer.method()
                    .name("finishModLoading")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(
                            InsnList().apply {
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/agent/platform/fabric/FabricPlatform",
                                        "finishModLoading",
                                        "()V"
                                    )
                                )
                            }
                        )
                    }
            }

            "net/fabricmc/loader/impl/discovery/ModDiscoverer" -> {
                info.markChanged()
                info.computeMaxStacks()

                // Prevent the agent from being loaded as a mod (important)
                MethodPointer.method()
                    .name($$"lambda$discoverMods$0")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(InsnList().apply {
                            add(VarInsnNode(Opcodes.ALOAD, 4))
                            add(MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "net/typho/big_shot/agent/platform/fabric/FabricPlatform",
                                "stripModCandidatePaths",
                                "(Ljava/util/List;)Ljava/util/List;"
                            ))
                            add(VarInsnNode(Opcodes.ASTORE, 4))
                        })
                    }
            }
        }
    }

    @JvmStatic
    fun stripModCandidatePaths(paths: List<Path>) = paths.filter { it != BigShotAgent.AGENT_PATH }

    @JvmStatic
    fun testParentURL(url: URL, fileName: String): Boolean {
        val path = LoaderUtil.normalizeExistingPath(UrlUtil.getCodeSource(url, fileName))
        return path == BigShotAgent.AGENT_PATH
    }

    @JvmStatic
    fun getRawClassByteArray(bytes: ByteArray, url: URL, fileName: String): ByteArray {
        val className = fileName.substringBeforeLast(".class")

        try {
            var mod: PlatformMod? = null

            try {
                mod = getModAt(LoaderUtil.normalizeExistingPath(UrlUtil.getCodeSource(url, fileName)))
            } catch (t: Throwable) {
                LOG.error("Error finding owner mod for class $className", t)
            }

            val info = ClassTransformInfo.ByteTransform(bytes)

            BigShotAgent.TRANSFORM_EVENTS.execute { id, event ->
                info.fallbackErrorSource = id
                event.transform(mod, info)
            }

            return info.compile(BigShotAgent::debugSaveClass) ?: bytes
        } catch (t: Throwable) {
            LOG.error("Error transforming class $className\nTransform event graph:\n${BigShotAgent.TRANSFORM_EVENTS}", t)

            return bytes
        }
    }

    @JvmStatic
    fun finishModLoading() {
        loadBigShotMetadata()
        loaded = true
    }

    object CandidateFinder : ModCandidateFinder {
        override fun findCandidates(consumer: ModCandidateFinder.ModCandidateConsumer) {
            consumer.accept(BigShotAgent.API_PATH, false)
        }
    }
}