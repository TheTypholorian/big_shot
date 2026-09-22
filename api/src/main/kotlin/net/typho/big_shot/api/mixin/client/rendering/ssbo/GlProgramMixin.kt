package net.typho.big_shot.api.mixin.client.rendering.ssbo

import com.llamalad7.mixinextras.sugar.Local
import com.llamalad7.mixinextras.sugar.Share
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef
import com.mojang.blaze3d.opengl.GlProgram
import com.mojang.blaze3d.opengl.Uniform
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.shaders.UniformType
import net.typho.big_shot.api.client.rendering.ssbo.SsboUniform
import net.typho.big_shot.api.mixin.jumps.Jump
import net.typho.big_shot.api.mixin.jumps.JumpInfo
import org.lwjgl.opengl.GL43.*
import org.objectweb.asm.Opcodes
import org.slf4j.Logger
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Debug(export = true)
@Mixin(GlProgram::class)
abstract class GlProgramMixin {
    companion object {
        @Shadow
        @Final
        private lateinit var LOGGER: Logger
    }

    @Shadow
    @Final
    private var programId: Int = 0
    @Shadow
    @Final
    private lateinit var debugLabel: String

    @Inject(
        method = ["setupBindGroupLayouts"],
        at = [At(
            value = "BIG_SHOT:SWITCH"
        )]
    )
    private fun setupBindGroupLayouts(
        ci: CallbackInfo,
        @Jump(
            value = At(
                value = "JUMP",
                opcode = Opcodes.IFNONNULL
            ),
            localsToModify = [
                Local(type = Uniform::class)
            ],
            shiftBeforeStack = true
        ) jump: JumpInfo.Complex,
        @Local uniform: BindGroupLayout.UniformDescription,
        @Share("nextSsboBinding") nextSsboBinding: LocalIntRef
    ) {
        if (uniform.type == UniformType.BIG_SHOT_SSBO) {
            val index = glGetProgramResourceIndex(programId, GL_SHADER_STORAGE_BLOCK, uniform.name)

            jump.jump()
            jump.setLocal(0, if (index == -1) {
                LOGGER.warn("$debugLabel shader program does not use ssbo ${uniform.name} defined in the pipeline. This might be a bug.")
                null
            } else {
                val binding = nextSsboBinding.get()
                nextSsboBinding.set(binding + 1)
                glShaderStorageBlockBinding(programId, index, binding)
                SsboUniform(binding)
            })
        }
    }
}