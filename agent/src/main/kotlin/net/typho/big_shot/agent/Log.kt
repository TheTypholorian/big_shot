package net.typho.big_shot.agent

import java.io.PrintWriter
import java.io.StringWriter
import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal var LOG: ILog = SystemLogImpl

internal object SystemLogImpl : ILog {
    private fun merge(msg: Any?, e: Throwable): String {
        val writer = StringWriter()
        writer.write(msg.toString())
        writer.write('\n'.code)
        e.printStackTrace(PrintWriter(writer, false))
        return writer.toString()
    }

    override fun error(msg: Any?) {
        System.err.println("[BigShot/ERROR] $msg")
    }

    override fun error(msg: Any?, e: Throwable) {
        error(merge(msg, e))
    }

    override fun warn(msg: Any?) {
        println("[BigShot/WARN] $msg")
    }

    override fun warn(msg: Any?, e: Throwable) {
        warn(merge(msg, e))
    }

    override fun info(msg: Any?) {
        println("[BigShot/INFO] $msg")
    }

    override fun info(msg: Any?, e: Throwable) {
        info(merge(msg, e))
    }

    override fun debug(msg: Any?) {
        println("[BigShot/DEBUG] $msg")
    }

    override fun debug(msg: Any?, e: Throwable) {
        debug(merge(msg, e))
    }

    override fun trace(msg: Any?) {
        println("[BigShot/TRACE] $msg")
    }

    override fun trace(msg: Any?, e: Throwable) {
        trace(merge(msg, e))
    }
}

internal interface ILog {
    fun error(msg: Any?)

    fun error(msg: Any?, e: Throwable)

    fun warn(msg: Any?)

    fun warn(msg: Any?, e: Throwable)

    fun info(msg: Any?)

    fun info(msg: Any?, e: Throwable)

    fun debug(msg: Any?)

    fun debug(msg: Any?, e: Throwable)

    fun trace(msg: Any?)

    fun trace(msg: Any?, e: Throwable)
}