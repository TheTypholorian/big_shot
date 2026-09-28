package net.typho.big_shot.common.gradle

import net.typho.big_shot.common.env.Dist

interface ModGradleEntrypoint {
    fun createConfig(properties: Map<String, Any>): Config

    data class Config(
        @JvmField
        val id: String,
        @JvmField
        val version: String,

        @JvmField
        val display: DisplayInfo = DisplayInfo(),
        @JvmField
        val runtime: RuntimeInfo = RuntimeInfo()
    ) {
        data class DisplayInfo(
            @JvmField
            val name: String? = null,
            @JvmField
            val description: String? = null,
            @JvmField
            val sources: String? = null,
            @JvmField
            val issues: String? = null,
            @JvmField
            val authors: List<String> = listOf(),
            @JvmField
            val license: String? = null,
            @JvmField
            val icon: String? = null,
        )

        data class RuntimeInfo(
            @JvmField
            val dist: Dist = Dist.UNIVERSAL,
            @JvmField
            val entrypoints: Map<String, List<String>> = mapOf(),
            @JvmField
            val mixins: List<String> = listOf(),
            @JvmField
            val classTweaker: String? = null,
        )
    }
}