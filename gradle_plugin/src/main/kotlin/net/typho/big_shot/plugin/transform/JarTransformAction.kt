package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.error.ClassVisitException
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

    fun createTransformer(): Transformer

    override fun transform(outputs: TransformOutputs) {
        val input = input.get().asFile

        try {
            val output = outputs.file(input.nameWithoutExtension + "-transformed.jar")
            val transformer = createTransformer()

            JarFile(input).use { inJar ->
                JarOutputStream(output.outputStream()).use { outJar ->
                    inJar.entries().asIterator().forEach { entry ->
                        if (entry.name.endsWith(".class")) {
                            try {
                                val bytes = inJar.getInputStream(entry).readAllBytes()
                                val info = ClassTransformInfo.ByteTransform(bytes)
                                transformer.transformClass(info)
                                outJar.putNextEntry(entry)
                                outJar.write(info.compile() ?: bytes)
                                outJar.closeEntry()
                            } catch (t: Throwable) {
                                throw ClassVisitException("Error transforming class ${entry.name}", t)
                            }
                        } else {
                            outJar.putNextEntry(entry)
                            transformer.transformMiscFile(entry, inJar.getInputStream(entry), outJar)
                            outJar.closeEntry()
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            RuntimeException("Error transforming library $input", t).printStackTrace()
        }
    }

    interface Transformer {
        fun transformClass(info: ClassTransformInfo)

        fun transformMiscFile(entry: JarEntry, input: InputStream, output: OutputStream) {
            input.transferTo(output)
        }
    }

    interface NoParameters : TransformParameters
}