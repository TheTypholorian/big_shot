package net.typho.big_shot.plugin.transform

import net.typho.asm_util.ASMUtil
import net.typho.asm_util.ClassTransformInfo
import net.typho.asm_util.method.MethodPointer
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault
import org.objectweb.asm.Opcodes

@DisableCachingByDefault
abstract class AccessWidenTransformAction : JarTransformAction<JarTransformAction.NoParameters> {
    override fun transformClass(info: ClassTransformInfo) {
        when (info.className) {
            "com/llamalad7/mixinextras/sugar/impl/SugarApplicator" -> {
                info.node.access = ASMUtil.accessPublic(info.node.access)
                info.node.fields.forEach { it.access = ASMUtil.accessPublic(it.access) }
                info.node.methods.forEach { it.access = ASMUtil.accessPublic(it.access) }
                info.markChanged()
            }
            "com/llamalad7/mixinextras/sugar/impl/SugarPostProcessingExtension" -> {
                MethodPointer.method().name("enqueuePostProcessing").find(info.node).forEach { it.access = ASMUtil.accessPublic(it.access) }
                info.markChanged()
            }
        }
    }
}