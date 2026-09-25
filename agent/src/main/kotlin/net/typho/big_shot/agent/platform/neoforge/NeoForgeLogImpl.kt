package net.typho.big_shot.agent.platform.neoforge

import net.typho.big_shot.agent.BigShotAgent
import net.typho.big_shot.agent.ILog
import org.slf4j.LoggerFactory

internal object NeoForgeLogImpl : ILog {
    private val logger = LoggerFactory.getLogger(BigShotAgent::class.java)

    override fun error(msg: Any?) {
        logger.error(msg.toString())
    }

    override fun error(msg: Any?, e: Throwable) {
        logger.error(msg.toString(), e)
    }

    override fun warn(msg: Any?) {
        logger.warn(msg.toString())
    }

    override fun warn(msg: Any?, e: Throwable) {
        logger.warn(msg.toString(), e)
    }

    override fun info(msg: Any?) {
        logger.info(msg.toString())
    }

    override fun info(msg: Any?, e: Throwable) {
        logger.info(msg.toString(), e)
    }

    override fun debug(msg: Any?) {
        logger.debug(msg.toString())
    }

    override fun debug(msg: Any?, e: Throwable) {
        logger.debug(msg.toString(), e)
    }

    override fun trace(msg: Any?) {
        logger.trace(msg.toString())
    }

    override fun trace(msg: Any?, e: Throwable) {
        logger.trace(msg.toString(), e)
    }
}