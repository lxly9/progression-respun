package com.gayasslily.progression_respun.mixin.waterlogging;

import com.gayasslily.progression_respun.ProgressionRespun;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.ToIntFunction;

@Mixin(Blocks.class)
public class BlocksMixin {

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$Settings;luminance(Ljava/util/function/ToIntFunction;)Lnet/minecraft/block/AbstractBlock$Settings;", ordinal = 2))
    private static AbstractBlock.Settings progressionrespun$torchLuminance(AbstractBlock.Settings instance, ToIntFunction<BlockState> luminance) {
        return instance.luminance(ProgressionRespun::getLuminance);
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$Settings;luminance(Ljava/util/function/ToIntFunction;)Lnet/minecraft/block/AbstractBlock$Settings;", ordinal = 3))
    private static AbstractBlock.Settings progressionrespun$wallTorchLuminance(AbstractBlock.Settings instance, ToIntFunction<BlockState> luminance) {
        return instance.luminance(ProgressionRespun::getLuminance);
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$Settings;luminance(Ljava/util/function/ToIntFunction;)Lnet/minecraft/block/AbstractBlock$Settings;", ordinal = 10))
    private static AbstractBlock.Settings progressionrespun$soulTorchLuminance(AbstractBlock.Settings instance, ToIntFunction<BlockState> luminance) {
        return instance.luminance(ProgressionRespun::getSoulLuminance);
    }

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$Settings;luminance(Ljava/util/function/ToIntFunction;)Lnet/minecraft/block/AbstractBlock$Settings;", ordinal = 11))
    private static AbstractBlock.Settings progressionrespun$soulWallTorchLuminance(AbstractBlock.Settings instance, ToIntFunction<BlockState> luminance) {
        return instance.luminance(ProgressionRespun::getSoulLuminance);
    }
}
