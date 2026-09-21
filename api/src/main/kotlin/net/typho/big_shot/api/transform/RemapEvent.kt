package net.typho.big_shot.api.transform

import net.typho.asm_util.ClassTransformInfo
import org.objectweb.asm.ClassVisitor

fun interface RemapEvent {
    fun createVisitor(
        info: ClassTransformInfo,
        classVisitor: ClassVisitor
    ): ClassVisitor?
}