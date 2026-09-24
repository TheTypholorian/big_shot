package net.typho.big_shot.plugin

import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.typho.big_shot.common.BigShotModData
import net.typho.big_shot.common.ct.ClassTweakers
import net.typho.big_shot.plugin.transform.AccessWidenTransformAction
import net.typho.big_shot.plugin.transform.MinecraftTransformAction
import net.typho.data_util.impl.JsonFormat
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.LocalArtifactRequest
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.plugins.JavaPluginExtension
import sun.util.calendar.CalendarUtils.mod
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writer

class BigShotPlugin : Plugin<Project> {
    companion object {
        @JvmField
        val MINECRAFT_TRANSFORMED_ATTRIBUTE = Attribute.of(
            "big_shot.minecraft_transformed",
            Boolean::class.javaObjectType
        )
        @JvmField
        val ACCESS_WIDENED_ATTRIBUTE = Attribute.of(
            "big_shot.access_widened",
            Boolean::class.javaObjectType
        )
    }

    override fun apply(project: Project) {
        val cacheFolder = project.gradle.gradleUserHomeDir.resolve("caches").resolve("big_shot")
        val ext = project.extensions.create("bigShot", BigShotBuildService::class.java, cacheFolder)

        project.plugins.apply("java")
        val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)

        project.dependencies.artifactTypes.configureEach {
            it.attributes.attribute(MINECRAFT_TRANSFORMED_ATTRIBUTE, false)
            it.attributes.attribute(ACCESS_WIDENED_ATTRIBUTE, false)
        }
        project.dependencies.registerTransform(MinecraftTransformAction::class.java) {
            it.from.attribute(MINECRAFT_TRANSFORMED_ATTRIBUTE, false)
            it.to.attribute(MINECRAFT_TRANSFORMED_ATTRIBUTE, true)
        }
        project.dependencies.registerTransform(AccessWidenTransformAction::class.java) {
            it.from.attribute(ACCESS_WIDENED_ATTRIBUTE, false)
            it.to.attribute(ACCESS_WIDENED_ATTRIBUTE, true)
            it.parameters.classTweakers.set(project.provider {
                val classTweakers = mutableListOf<File>()
                javaExt.sourceSets.forEach { it.resources.sourceDirectories.forEach {
                    val metadata = it.resolve("big_shot.mod.json")

                    if (metadata.exists()) {
                        val data = JsonFormat().read(BigShotModData.CODEC, metadata.readText())

                        data.classTweaker?.let { classTweaker ->
                            val file = it.resolve(classTweaker)

                            if (file.exists()) {
                                classTweakers.add(file)
                            } else {
                                System.err.println("Class tweaker ${data.classTweaker} does not exist (should be at $file)")
                            }
                        }
                    }
                } }
                classTweakers
            })
        }

        val extraAccessWiden = project.configurations.create("extraAccessWiden")
        project.dependencies.add("implementation", extraAccessWiden.incoming.artifactView {
            it.attributes.attribute(ACCESS_WIDENED_ATTRIBUTE, true)
        }.artifacts.artifactFiles)

        val minecraft = project.configurations.create("minecraft")
        project.dependencies.add("implementation", minecraft.incoming.artifactView {
            it.attributes.attribute(MINECRAFT_TRANSFORMED_ATTRIBUTE, true)
            it.attributes.attribute(ACCESS_WIDENED_ATTRIBUTE, true)
        }.artifacts.artifactFiles)

        val repoPath = cacheFolder.resolve("minecraft_repo").toPath()
        val repoSystem = RepositorySystemSupplier().get()
        val repoSession = repoSystem.createSessionBuilder().withLocalRepositories(LocalRepository(repoPath)).build()

        project.repositories.maven {
            it.setUrl(repoPath)
        }
        project.repositories.maven {
            it.setUrl("https://libraries.minecraft.net")
        }

        val version = ext.versionManifest.getFamily("26.2")

        val artifact = DefaultArtifact(
            "com.mojang",
            "minecraft",
            "jar",
            version.primaryVersion
        )

        if (!repoSession.localRepositoryManager.find(repoSession, LocalArtifactRequest().setArtifact(artifact)).isAvailable) {
            val temp = Files.createTempDirectory("big_shot_minecraft_download")
            val tempJar = temp.resolve("client.jar")
            val tempPom = temp.resolve("client.pom")

            tempJar.writeBytes(URI.create(version.info.downloads.client.url).toURL().openConnection().getInputStream().readAllBytes())

            val model = Model()

            model.modelVersion = "4.0.0"
            model.groupId = artifact.groupId
            model.artifactId = artifact.artifactId
            model.version = artifact.version
            model.packaging = "jar"

            tempPom.writer().use {
                MavenXpp3Writer().write(it, model)
            }

            repoSystem.install(
                repoSession,
                InstallRequest()
                    .addArtifact(artifact.setPath(tempJar))
                    .addArtifact(DefaultArtifact(
                        "com.mojang",
                        "minecraft",
                        "pom",
                        version.primaryVersion
                    ).setPath(tempPom))
            )
        }

        project.dependencies.add("minecraft", "com.mojang:minecraft:${version.primaryVersion}")

        if (project.findProperty("big_shot.transitive_minecraft_dependencies") != "false") {
            for (lib in version.info.libraries) {
                project.dependencies.add("implementation", lib.name)
            }
        }
    }
}