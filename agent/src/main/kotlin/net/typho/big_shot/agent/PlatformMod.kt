package net.typho.big_shot.agent

import net.typho.big_shot.common.BigShotModData
import java.io.InputStream
import java.nio.file.Path

abstract class PlatformMod {
    abstract val id: String
    abstract val version: String
    abstract val bigShotData: BigShotModData?

    abstract fun findResource(file: String): InputStream?

    override fun toString(): String {
        return "$id $version"
    }
}