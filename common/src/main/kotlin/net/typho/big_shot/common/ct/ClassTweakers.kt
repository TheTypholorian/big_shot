package net.typho.big_shot.common.ct

import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor
import net.typho.asm_util.ClassTransformInfo
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

object ClassTweakers {
    @JvmField
    val EARLY = ClassTweaker.newInstance().apply {
        visitAccessWidener("net/fabricmc/loader/impl/discovery/ModCandidateFinder")!!.apply {
            visitClass(AccessWidenerVisitor.AccessType.ACCESSIBLE, false)
        }
    }

    @JvmStatic
    fun ClassTweaker.apply(info: ClassTransformInfo) {
        if (allAccessWideners.containsKey(info.className) || allEnumExtensions.containsKey(info.className) || allInjectedInterfaces.containsKey(info.className)) {
            val newNode = ClassNode()
            info.node.accept(createClassVisitor(
                Opcodes.ASM9,
                newNode,
                null
            ))
            info.node = newNode
            info.markChanged()
        }
    }
}