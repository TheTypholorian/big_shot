import net.typho.big_shot.common.gradle.ModGradleEntrypoint
import net.typho.big_shot.common.gradle.ModGradleEntrypoint.Config

object Config : ModGradleEntrypoint {
	override fun createConfig(properties: Map<String, Any>) = Config(
		id = "big_shot_agent",
		version = properties["version"]!!.toString()
	)
}