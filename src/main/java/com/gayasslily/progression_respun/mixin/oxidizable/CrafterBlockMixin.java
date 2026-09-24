package com.gayasslily.progression_respun.mixin.oxidizable;

import com.gayasslily.progression_respun.ProgressionRespun;
import com.gayasslily.progression_respun.block.OxidizableCrafterBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.CrafterBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {
    @WrapOperation(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    private void progressionrespun$scheduleDelay(World instance, BlockPos blockPos, Block block, int i, Operation<Void> original) {
        if ((Object)this instanceof OxidizableCrafterBlock crafterBlock) original.call(instance, blockPos, block, ProgressionRespun.getDelayForOxidization(crafterBlock.getDegradationLevel()));
    }
}
