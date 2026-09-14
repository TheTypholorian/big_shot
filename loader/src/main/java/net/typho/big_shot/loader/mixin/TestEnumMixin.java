package net.typho.big_shot.loader.mixin;

import net.typho.big_shot.loader.TestMixinTarget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TestMixinTarget.TestEnum.class)
public enum TestEnumMixin {
    BIG_SHOT_LOADER_Z
}
