package net.typho.big_shot.agent.platform.fabric

import ca.weblite.objc.RuntimeUtils.cls
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.impl.discovery.ModCandidateFinder
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.LOG_INSTANCE
import net.typho.big_shot.agent.Log
import net.typho.big_shot.agent.TestInterface
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.common.KtServiceLoader
import net.typho.big_shot.common.KtServiceLoader.loadAll
import net.typho.big_shot.common.event.EventGraph
import org.jetbrains.annotations.ApiStatus
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.util.ServiceLoader
import kotlin.io.path.readText
import kotlin.io.path.reader
import kotlin.jvm.optionals.getOrNull

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
                val metadata = mod.findPath("big_shot.mod.json")
                println("$mod $metadata")
                metadata.ifPresent { println("\t${it.readText()}") }
            } catch (t: Throwable) {
                Log.error("Error while loading big shot mod metadata for $mod", t)
            }
        }

        loadModService(TestInterface::class.java).loadAll().forEach { (mod, services) ->
            println("$mod: $services")
            services.forEach { it.abc() }
        }
    }

    @JvmStatic
    fun <S : Any> Map<ModContainer, List<ServiceLoader.Provider<S>>>.loadAll() = mapValues { it.value.loadAll() }

    @JvmOverloads
    @JvmStatic
    fun <S : Any> loadModService(service: Class<S>, loader: ClassLoader = FabricLauncherBase.getLauncher().targetClassLoader ?: Thread.currentThread().contextClassLoader): Map<ModContainer, List<ServiceLoader.Provider<S>>> {
        println("loader $loader")
        return FabricLoader.getInstance().allMods.associateWith { mod ->
            val path = mod.findPath(KtServiceLoader.PREFIX + service.name).getOrNull() ?: return@associateWith listOf()
            KtServiceLoader.load(service, path.reader().readAllLines().filter { it.isNotBlank() }, loader)
        }.filterValues { !it.isEmpty() }
    }

    object CandidateFinder : ModCandidateFinder {
        override fun findCandidates(consumer: ModCandidateFinder.ModCandidateConsumer) {
            consumer.accept(BigShotAgent.API_PATH, false)
        }
    }
}