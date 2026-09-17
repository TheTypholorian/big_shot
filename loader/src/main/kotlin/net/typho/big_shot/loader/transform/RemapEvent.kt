package net.typho.big_shot.loader.transform

import net.typho.asm_util.ClassTransformInfo
import org.objectweb.asm.commons.Remapper

fun interface RemapEvent {
    fun createRemapper(
        info: ClassTransformInfo
    ): Remapper?
}