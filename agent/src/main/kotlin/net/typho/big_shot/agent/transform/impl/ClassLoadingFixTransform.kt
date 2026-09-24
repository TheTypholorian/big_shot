package net.typho.big_shot.agent.transform.impl

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.method.MethodPointer
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.transform.TransformEvent
import net.typho.big_shot.common.event.EventGraph
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * Transforms [net.fabricmc.loader.impl.launch.knot.KnotClassDelegate] to allow loading of agent classes from the main process
 */
@Suppress("unused")
object ClassLoadingFixTransform : TransformEvent.KnownTargets("net/fabricmc/loader/impl/launch/knot/KnotClassDelegate"), EventGraph.SelfAware<String> {
    override val id: String
        get() = "big_shot:class_loading_fix"

    override fun transformImpl(
        mod: PlatformMod?,
        info: ClassTransformInfo
    ) {
        info.markChanged()
        info.computeFrames()

        MethodPointer.method().name("isValidParentUrl").findOrThrow(info.node) { method ->
            method.instructions.insert(InsnList().apply {
                val label = LabelNode()
                add(VarInsnNode(Opcodes.ALOAD, 2))
                add(MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "net/typho/big_shot/agent/transform/impl/ClassLoadingFixTransform",
                    "test",
                    "(Ljava/lang/String;)Z"
                ))
                add(JumpInsnNode(Opcodes.IFEQ, label))
                add(InsnNode(Opcodes.ICONST_1))
                add(InsnNode(Opcodes.IRETURN))
                add(label)
            })
        }
    }

    @JvmStatic
    fun test(fileName: String): Boolean {
        return fileName.startsWith("net/typho/big_shot/agent") // TODO
        /*
        return try {
            println("testing $url")
            val path = url.toURI().toPath()
            println("testing path $path")
            val r = url.toURI().toPath().startsWith(BigShotAgent.AGENT_PATH)
            println("result $r")
            r
        } catch (e: FileSystemNotFoundException) {
            false
        }
         */
    }
}