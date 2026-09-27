package net.typho.big_shot.agent.entrypoint

import net.typho.asm_util.ClassTransformInfo
import net.typho.big_shot.agent.PlatformMod
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList

interface EntrypointInputSupplier {
    fun getInput(entrypointKey: String, owner: PlatformMod, info: ClassTransformInfo, field: FieldNode): InsnList?
}