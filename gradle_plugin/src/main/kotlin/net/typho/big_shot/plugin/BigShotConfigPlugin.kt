package net.typho.big_shot.plugin

import net.typho.big_shot.plugin.ext.BigShotConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class BigShotConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("applying config to ${project.name}")
        project.group = "big_shot.config"
        project.version = "1.0.0"

        val ext = project.extensions.create("bigShot", BigShotConfigExtension::class.java)

        project.repositories.mavenCentral()
        project.plugins.apply("java")
        project.plugins.apply("java-gradle-plugin")
        val pluginExt = project.extensions.getByType(GradlePluginDevelopmentExtension::class.java)

        project.tasks.named("compileJava", JavaCompile::class.java) {
            val dir = project.layout.buildDirectory.dir("generated/java")

            it.doFirst {
                val outDir = dir.get().asFile
                outDir.mkdirs()

                outDir.resolve("DummyPluginClass.java").writeText("""
                    import org.gradle.api.Plugin;
                    import org.gradle.api.Project;
                    
                    public class DummyPluginClass implements Plugin<Project> {
                        public void apply(Project project) {
                        }
                    }
                """.trimIndent())
            }

            it.source(dir)
        }

        pluginExt.plugins.create("dev_plugin") {
            it.id = "big_shot.config.${project.name}"
            it.implementationClass = "DummyPluginClass"
        }

        project.repositories.maven { it.setUrl("https://maven.fabricmc.net") }
        project.repositories.maven { it.setUrl("https://typho.net/maven") }
        project.dependencies.add("implementation", "net.typho:big_shot.common:${Constants.COMMON_VERSION}")

        project.afterEvaluate {
            val configClass = ext.configClass.get().replace('/', '.')
            project.tasks.withType(Jar::class.java).configureEach {
                it.manifest {
                    it.attributes(mapOf(
                        Constants.MANIFEST_CONFIG_CLASS to configClass
                    ))
                }
            }
        }
    }
}