package net.typho.big_shot.loader.mixin.client.rendering.ssbo

import com.llamalad7.mixinextras.sugar.Local
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.opengl.GlBuffer
import com.mojang.blaze3d.opengl.GlRenderPass
import com.mojang.blaze3d.opengl.Uniform
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.shaders.UniformType
import net.typho.big_shot.loader.client.rendering.buffer.GpuBufferUsage
import net.typho.big_shot.loader.client.rendering.ssbo.SsboUniform
import net.typho.big_shot.loader.mixin.jumps.Jump
import net.typho.big_shot.loader.mixin.jumps.JumpInfo
import org.lwjgl.opengl.GL30.glBindBufferRange
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER
import org.objectweb.asm.Opcodes
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Debug(export = true)
@Mixin(targets = ["com/mojang/blaze3d/opengl/GlCommandEncoder"])
class GlCommandEncoderMixin {
    @Inject(
        method = ["trySetup"],
        at = [At(
            value = "INVOKE",
            target = $$"Lcom/mojang/blaze3d/pipeline/BindGroupLayout$UniformDescription;type()Lcom/mojang/blaze3d/shaders/UniformType;",
            ordinal = 1
        )]
    )
    private fun trySetup(
        cir: CallbackInfoReturnable<Boolean>,
        @Local(ordinal = 0) uniform: BindGroupLayout.UniformDescription,
        @Local(ordinal = 0) value: GpuBufferSlice
    ) {
        if (uniform.type == UniformType.BIG_SHOT_SSBO) {
            if (value.buffer.isClosed) {
                throw IllegalStateException("Shader storage buffer ${uniform.name} is already closed")
            }

            if ((value.buffer.usage() and GpuBufferUsage.SHADER_STORAGE.flags) == 0) {
                throw IllegalStateException("Shader storage buffer ${uniform.name} must have GpuBufferUsage.SHADER_STORAGE")
            }
        }
    }

    @Inject(
        method = ["trySetup"],
        at = [At(
            value = "BIG_SHOT:SWITCH"
        )]
    )
    private fun trySetup(
        renderPass: GlRenderPass,
        dynamicUniforms: Collection<String>,
        cir: CallbackInfoReturnable<Boolean>,
        @Local entry: Map.Entry<String, Uniform>,
        @Jump(value = At(
            value = "INVOKE",
            target = "Ljava/util/Iterator;hasNext()Z",
            ordinal = 2
        ), shiftBeforeStack = true) jump: JumpInfo
    ) {
        if (entry.value is SsboUniform && renderPass.dirtyUniforms.contains(entry.key)) {
            val buffer = renderPass.uniforms[entry.key]!!
            glBindBufferRange(GL_SHADER_STORAGE_BUFFER, (entry.value as SsboUniform).binding, (buffer.buffer() as GlBuffer).handle(), buffer.offset(), buffer.length())
            jump.jump()
        }
    }

    companion object {
        @Inject(
            method = [$$"lambda$executeDrawMultiple$0"],
            at = [At(
                value = "BIG_SHOT:TYPE",
                opcode = Opcodes.INSTANCEOF
            )],
            cancellable = true
        )
        @JvmStatic
        private fun executeDrawMultiple(
            renderPass: GlRenderPass,
            name: String,
            buffer: GpuBufferSlice,
            ci: CallbackInfo,
            @Local uniform: Uniform
        ) {
            test()
            if (uniform is SsboUniform) {
                glBindBufferRange(GL_SHADER_STORAGE_BUFFER, uniform.binding, (buffer.buffer() as GlBuffer).handle(), buffer.offset(), buffer.length())

                ci.cancel()
            }
        }

        @JvmStatic
        @Unique
        private fun test() {
            println("yay")
        }
    }
}