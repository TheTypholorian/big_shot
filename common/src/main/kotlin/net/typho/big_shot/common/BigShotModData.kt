package net.typho.big_shot.common

import net.typho.data_util.codec.Codec

data class BigShotModData(
    @JvmField
    val classTweaker: String?
) {
    companion object {
        const val FILE_NAME = "big_shot.mod.json"
        @JvmField
        val CODEC = Codec.reflect(BigShotModData::class.java)
    }
}