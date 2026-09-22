package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.impl.discovery.ModCandidateFinder
import net.fabricmc.loader.impl.game.GameProvider
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.util.event.EventGraph
import org.jetbrains.annotations.ApiStatus
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.spongepowered.asm.mixin.Mixins

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
                                "clinit",
                                "()V"
                            )
                        )
                    }
                MethodPointer.method()
                    .name("init")
                    .desc("([Ljava/lang/String;)Ljava/lang/ClassLoader;")
                    .findOrThrow(info.node) { method ->
                        method.instructions.insert(
                            InsnPointer.fieldSet()
                                .owner(info.className)
                                .name("provider")
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(VarInsnNode(Opcodes.ALOAD, 0))
                                add(
                                    FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        info.className,
                                        "provider",
                                        "Lnet/fabricmc/loader/impl/game/GameProvider;"
                                    )
                                )
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/agent/platform/fabric/BigShotFabric",
                                        "loadGameProvider",
                                        "(Lnet/fabricmc/loader/impl/game/GameProvider;)V"
                                    )
                                )
                            }
                        )
                        method.instructions.insert(
                            InsnPointer.methodCallStatic()
                                .owner("net/fabricmc/loader/impl/launch/FabricLauncherBase")
                                .name("finishMixinBootstrapping")
                                .findOrThrow(method.instructions),
                            InsnList().apply {
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/typho/big_shot/agent/platform/fabric/BigShotFabric",
                                        "registerMixins",
                                        "()V"
                                    )
                                )
                            }
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
    fun clinit() {
        println("loaded into a bright future with mucho shenanigans to come")
    }

    @JvmStatic
    fun loadGameProvider(provider: GameProvider) {
        println("game provider $provider, name ${provider.gameName} and version ${provider.rawGameVersion}")
    }

    @JvmStatic
    fun registerMixins() {
        println("registering mixins")
        //Mixins.addConfiguration("big_shot.mixins.json")
    }

    @JvmStatic
    fun finishModLoading() {
    }

    object CandidateFinder : ModCandidateFinder {
        override fun findCandidates(consumer: ModCandidateFinder.ModCandidateConsumer) {
            consumer.accept(BigShotAgent.API_PATH, false)
        }
    }
}