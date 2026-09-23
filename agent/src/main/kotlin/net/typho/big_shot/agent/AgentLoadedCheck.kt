package net.typho.big_shot.agent

import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.nio.file.Path
import javax.swing.JOptionPane
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

internal object AgentLoadedCheck {
    @JvmField
    var loaded = false

    @JvmStatic
    fun check(paths: List<Path>) {
        if (!loaded) {
            var message = "The Big Shot library requires its java agent to be loaded."
            val options = mutableListOf<Pair<String, () -> Unit>>()

            if (paths.size == 1) {
                val arg = "\"-javaagent:${paths.single().absolutePathString()}\""
                message += "\nThis can be solved by adding the java argument\n$arg\nto your Minecraft instance."
                options.add("Copy Argument" to { Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(arg), null) })
            } else {
                message += "\nPlease ask in the typho.net discord server (https://typho.net/discord) for help."
                message += "\n\tNon-singular set of mod origin paths:"

                for (path in paths) {
                    message += "\n\t- ${path.absolutePathString()}"
                }

                options.add("Discord" to { Desktop.getDesktop().browse(URI("https://typho.net/discord")) })
            }

            options.add("Close" to { exitProcess(1) })

            val isCI = System.getenv("CI") != null

            if (!isCI && !GraphicsEnvironment.isHeadless()) {
                val result = JOptionPane.showOptionDialog(
                    null,
                    message,
                    "The Big Shot java agent was not loaded",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options.map { it.first }.toTypedArray(),
                    "Close"
                )
                options[result].second()
            } else {
                throw RuntimeException(message)
            }
        }
    }
}