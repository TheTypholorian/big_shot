package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ClassTransformInfo
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.InputStream
import java.io.OutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

interface JarTransformAction<P : TransformParameters> : TransformAction<P> {
    @get:InputArtifact
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val input: Provider<FileSystemLocation>

    fun transformClass(info: ClassTransformInfo)

    fun transformMiscFile(entry: JarEntry, input: InputStream, output: OutputStream) {
        input.transferTo(output)
    }

    override fun transform(outputs: TransformOutputs) {
        val input = input.get().asFile
        val output = outputs.file(input.nameWithoutExtension + "-transformed.jar")

        JarFile(input).use { inJar ->
            JarOutputStream(output.outputStream()).use { outJar ->
                inJar.entries().asIterator().forEach { entry ->
                    if (entry.name.endsWith(".class")) {
                        val bytes = inJar.getInputStream(entry).readAllBytes()
                        val info = ClassTransformInfo.ByteTransform(bytes)
                        transformClass(info)
                        outJar.putNextEntry(entry)
                        outJar.write(info.compile() ?: bytes)
                        outJar.closeEntry()
                    } else {
                        outJar.putNextEntry(entry)
                        transformMiscFile(entry, inJar.getInputStream(entry), outJar)
                        outJar.closeEntry()
                    }
                }
            }
        }
    }

    interface NoParameters : TransformParameters
}