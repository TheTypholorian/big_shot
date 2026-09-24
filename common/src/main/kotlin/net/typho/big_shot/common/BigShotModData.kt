package net.typho.big_shot.common

import net.typho.data_util.codec.Codec

data class BigShotModData(
    @JvmField
    val classTweaker: String?
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect(BigShotModData::class.java)
    }
}