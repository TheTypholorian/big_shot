package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import net.fabricmc.loader.impl.discovery.ModCandidateFinder
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.LOG_INSTANCE
import net.typho.big_shot.agent.Log
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.common.event.EventGraph
import org.jetbrains.annotations.ApiStatus
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.exists

@ApiStatus.Internal
@Suppress("unused")
object BigShotFabric : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:platform/fabric"

    override fun transform(
        type: TransformSource,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "net/fabricmc/loader/impl/launch/knot/Knot" -> {
                info.markChanged()
                info.computeMaxStacks()

                MethodPointer.method()
                    .name("<clinit>")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insertBefore(
                            InsnPointer.simple()
                                .opcode(Opcodes.RETURN)
                                .findOrThrow(method.instructions),
                            MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "net/typho/big_shot/agent/platform/fabric/BigShotFabric",
                                "init",
                                "()V"
                            )
                        )
                    }
            }

            "net/fabricmc/loader/impl/FabricLoaderImpl" -> {
                info.markChanged()
                info.computeMaxStacks()

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
                                add(
                                    FieldInsnNode(
                                        Opcodes.GETSTATIC,
                                        $$"net/typho/big_shot/agent/platform/fabric/BigShotFabric$CandidateFinder",
                                        "INSTANCE",
                                        $$"Lnet/typho/big_shot/agent/platform/fabric/BigShotFabric$CandidateFinder;"
                                    )
                                )
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        "net/fabricmc/loader/impl/discovery/ModDiscoverer",
                                        "addCandidateFinder",
                                        "(Lnet/fabricmc/loader/impl/discovery/ModCandidateFinder;)V"
                                    )
                                )
                            }
                        )
                    }

                MethodPointer.method()
                    .name("finishModLoading")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(
                            InsnList().apply {
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/agent/platform/fabric/BigShotFabric",
                                        "finishModLoading",
                                        "()V"
                                    )
                                )
                            }
                        )
                    }
            }
        }
    }

    @JvmStatic
    fun init() {
        LOG_INSTANCE = FabricLogImpl
        Log.info("Loading big shot on fabric")
    }

    @JvmStatic
    fun finishModLoading() {
        Log.info("Loading big shot mod metadata")

        for (mod in FabricLoader.getInstance().allMods) {
            try {
                val paths = getModPaths(mod.origin)

                for (uri in paths) {
                    val metadata = uri.resolve("big_shot.mod.json")

                    println(metadata)
                }
            } catch (t: Throwable) {
                Log.error("Error while loading big shot mod metadata for $mod", t)
            }
        }
    }

    private fun getModPaths(origin: ModOrigin): List<URI> {
        return when (origin.kind) {
            ModOrigin.Kind.PATH -> origin.paths.map { it.toUri() }
            ModOrigin.Kind.NESTED -> getModPaths(FabricLoader.getInstance().getModContainer(origin.parentModId).orElseThrow().origin)
                .map { it.resolve(origin.parentSubLocation).normalize() }
            else -> listOf()
        }
    }

    object CandidateFinder : ModCandidateFinder {
        override fun findCandidates(consumer: ModCandidateFinder.ModCandidateConsumer) {
            consumer.accept(BigShotAgent.API_PATH, false)
        }
    }
}