package net.typho.big_shot.data.ct

import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor
import net.typho.asm_util.ClassTransformInfo
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

object BuiltinClassTweaker {
    @JvmField
    val INSTANCE = ClassTweaker.newInstance().apply {
        visitAccessWidener("com/llamalad7/mixinextras/sugar/impl/SugarApplicator")!!.apply {
            visitClass(AccessWidenerVisitor.AccessType.ACCESSIBLE, false)
            visitMethod(
                "<init>",
                "(Lorg/spongepowered/asm/mixin/injection/struct/InjectionInfo;Lcom/llamalad7/mixinextras/sugar/impl/SugarParameter;)V",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
            visitMethod(
                "validate",
                $$"(Lorg/spongepowered/asm/mixin/injection/struct/Target;Lorg/spongepowered/asm/mixin/injection/struct/InjectionNodes$InjectionNode;)V",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
            visitMethod(
                "prepare",
                $$"(Lorg/spongepowered/asm/mixin/injection/struct/Target;Lorg/spongepowered/asm/mixin/injection/struct/InjectionNodes$InjectionNode;)V",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
            visitMethod(
                "inject",
                $$"(Lorg/spongepowered/asm/mixin/injection/struct/Target;Lorg/spongepowered/asm/mixin/injection/struct/InjectionNodes$InjectionNode;Lcom/llamalad7/mixinextras/injector/StackExtension;)V",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
            visitMethod(
                "postProcessingPriority",
                "()I",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
        }
        visitAccessWidener("com/llamalad7/mixinextras/sugar/impl/SugarPostProcessingExtension")!!.apply {
            visitMethod(
                "enqueuePostProcessing",
                "(Lcom/llamalad7/mixinextras/sugar/impl/SugarApplicator;Ljava/lang/Runnable;)V",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
        }
        visitAccessWidener("com/mojang/blaze3d/opengl/Uniform")!!.apply {
            visitClass(AccessWidenerVisitor.AccessType.EXTENDABLE, false)
        }
        visitAccessWidener("com/mojang/blaze3d/opengl/GlRenderPass")!!.apply {
            visitClass(AccessWidenerVisitor.AccessType.ACCESSIBLE, false)
            visitField(
                "uniforms",
                "Ljava/util/HashMap;",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
            visitField(
                "dirtyUniforms",
                "Ljava/util/Set;",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
            visitField(
                "pipeline",
                "Lcom/mojang/blaze3d/opengl/GlRenderPipeline;",
                AccessWidenerVisitor.AccessType.ACCESSIBLE,
                false
            )
        }
        visitEnumExtension("com/mojang/blaze3d/shaders/UniformType", "BIG_SHOT_SSBO", true)
    }

    @JvmStatic
    fun apply(info: ClassTransformInfo) {
        if (INSTANCE.allAccessWideners.containsKey(info.className) || INSTANCE.allEnumExtensions.containsKey(info.className) || INSTANCE.allInjectedInterfaces.containsKey(info.className)) {
            val newNode = ClassNode()
            info.node.accept(INSTANCE.createClassVisitor(
                Opcodes.ASM9,
                newNode,
                null
            ))
            info.node = newNode
            info.markChanged()
        }
    }
}