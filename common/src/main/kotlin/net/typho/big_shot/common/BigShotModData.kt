package net.typho.big_shot.common

import net.typho.data_util.anno.FieldCodec
import net.typho.data_util.codec.Codec

data class BigShotModData(
    @FieldCodec(owner = BigShotModData::class, value = "ENTRYPOINTS_CODEC")
    @JvmField
    val entrypoints: Map<String, List<String>>?,
    @JvmField
    val classTweaker: String?
) {
    companion object {
        const val FILE_NAME = "big_shot.mod.json"
        @JvmField
        val ENTRYPOINTS_CODEC = Codec.unboundedMap(Codec.either(Codec.STRING.listOf(), listOf(Codec.STRING.mapRead { listOf(it) })))
        @JvmField
        val CODEC = Codec.reflect(BigShotModData::class.java)
    }
}