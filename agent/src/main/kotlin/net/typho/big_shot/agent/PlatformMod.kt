package net.typho.big_shot.agent

import net.typho.big_shot.common.BigShotModData
import java.io.InputStream
import java.nio.file.Path

interface PlatformMod {
    val id: String
    val version: String
    val bigShotData: BigShotModData?

    fun findResource(file: String): InputStream?
}