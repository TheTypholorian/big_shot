package net.typho.big_shot.loader.transform

import net.typho.asm_util.ClassTransformInfo

fun interface TransformEvent {
    fun transform(
        type: TransformType,
        info: ClassTransformInfo
    )
}