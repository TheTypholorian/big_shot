package net.typho.big_shot.agent.platform.fabric

import net.fabricmc.loader.impl.util.log.Log
import net.fabricmc.loader.impl.util.log.LogCategory
import net.typho.big_shot.agent.ILog

internal object FabricLogImpl : ILog {
    private val category = LogCategory.createCustom("BigShot")

    override fun error(msg: Any?) {
        Log.error(category, msg.toString())
    }

    override fun error(msg: Any?, e: Throwable) {
        Log.error(category, msg.toString(), e)
    }

    override fun warn(msg: Any?) {
        Log.warn(category, msg.toString())
    }

    override fun warn(msg: Any?, e: Throwable) {
        Log.warn(category, msg.toString(), e)
    }

    override fun info(msg: Any?) {
        Log.info(category, msg.toString())
    }

    override fun info(msg: Any?, e: Throwable) {
        Log.info(category, msg.toString(), e)
    }

    override fun debug(msg: Any?) {
        Log.debug(category, msg.toString())
    }

    override fun debug(msg: Any?, e: Throwable) {
        Log.debug(category, msg.toString(), e)
    }

    override fun trace(msg: Any?) {
        Log.trace(category, msg.toString())
    }

    override fun trace(msg: Any?, e: Throwable) {
        Log.trace(category, msg.toString(), e)
    }
}