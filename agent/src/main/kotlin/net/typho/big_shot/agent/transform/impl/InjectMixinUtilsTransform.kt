package net.typho.big_shot.agent.transform.impl

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair
import com.llamalad7.mixinextras.sugar.impl.SugarApplicator
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.insn.InsnPointer
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.agent.transform.TransformSource
import net.typho.big_shot.util.event.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.spongepowered.asm.mixin.injection.InjectionPoint
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo

@Suppress("unused")
object InjectMixinUtilsTransform : EventGraph.SelfAware<String>, TransformEvent {
    override val id: String
        get() = "big_shot:mixin_utils"

    override fun transform(
        type: TransformSource,
        info: ClassTransformInfo
    ) {
        when (info.className) {
            "com/llamalad7/mixinextras/sugar/impl/SugarApplicator" -> {
                MethodPointer.method().name("<clinit>").findOrThrow(info.node) { method ->
                    InsnPointer.methodCallStatic().owner("java/util/Arrays").name("asList").ordinal(0).findOrThrow(method.instructions) { insn ->
                        method.instructions.insert(insn, MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "net/typho/big_shot/agent/transform/impl/InjectMixinUtilsTransform",
                            "registerSugars",
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
                            "net/typho/big_shot/agent/transform/impl/InjectMixinUtilsTransform",
                            "registerInjectors",
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
                            "net/typho/big_shot/agent/transform/impl/InjectMixinUtilsTransform",
                            "registerInjectionPoints",
                            "()V"
                        ))
                    }
                }
                info.markChanged()
                info.computeFrames()
            }
        }
    }

    @JvmStatic
    fun registerSugars(sugars: List<Pair<Class<out Annotation>, Class<out SugarApplicator>>>): List<Pair<Class<out Annotation>, Class<out SugarApplicator>>> {
        val sugars = sugars.toMutableList()
        BigShotAgent.REGISTER_MIXIN_INJECTORS_EVENTS.execute { it.registerSugars { anno, applicator -> sugars.add(Pair.of(anno, applicator)) } }
        return sugars
    }

    @JvmStatic
    fun registerInjectors() {
        BigShotAgent.REGISTER_MIXIN_INJECTORS_EVENTS.execute { it.registerInjectors { InjectionInfo.register(it) } }
    }

    @JvmStatic
    fun registerInjectionPoints() {
        BigShotAgent.REGISTER_MIXIN_INJECTORS_EVENTS.execute { it.registerInjectionPoints { InjectionPoint.register(it) } }
    }
}