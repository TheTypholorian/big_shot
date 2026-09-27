package net.typho.big_shot.api.entrypoint

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.PlatformMod
import net.typho.big_shot.agent.entrypoint.EntrypointInputSupplier
import net.typho.big_shot.agent.platform.BigShotPlatform
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import java.lang.instrument.Instrumentation

object BuiltinEntrypointInputs : EntrypointInputSupplier {
    @JvmField
    val INSTRUMENTATION_TYPE = Type.getType(Instrumentation::class.java)
    @JvmField
    val PLATFORM_MOD_TYPE = Type.getType(PlatformMod::class.java)

    override fun getInput(
        entrypointKey: String,
        owner: PlatformMod,
        info: ClassTransformInfo,
        field: FieldNode
    ): InsnList? {
        if (field.name.lowercase() == "mod" && field.desc == PLATFORM_MOD_TYPE.descriptor) {
            return InsnList().apply {
                add(LdcInsnNode(owner.id))
                add(MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "net/typho/big_shot/api/entrypoint/BuiltinEntrypointInputs",
                    "getMod",
                    "(Ljava/lang/String;)$PLATFORM_MOD_TYPE"
                ))
            }
        }

        if (field.desc == INSTRUMENTATION_TYPE.descriptor) {
            return InsnList().apply {
                add(FieldInsnNode(
                    Opcodes.GETSTATIC,
                    "net/typho/big_shot/agent/BigShotAgent",
                    "INSTRUMENTATION",
                    INSTRUMENTATION_TYPE.descriptor
                ))
            }
        }

        return null
    }

    @JvmStatic
    fun getMod(id: String) = BigShotPlatform.INSTANCE!!.allMods.first { it.id == id }
}