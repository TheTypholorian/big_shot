package net.typho.big_shot.agent

import net.typho.big_shot.common.BigShotModData
import java.io.InputStream

abstract class PlatformMod {
    abstract val id: String
    abstract val version: String
    abstract val bigShotData: BigShotModData?

    abstract fun findResource(file: String): InputStream?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlatformMod

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString() = id
}