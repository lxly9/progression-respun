package com.gayasslily.progression_respun.mixin.waterlogging;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(WallMountedBlock.class)
public class WallMountedBlockMixin {

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState progressionrespun$modifyPlacementState(BlockState original, ItemPlacementContext ctx) {
        if (original != null) {
            return original.contains(WATERLOGGED) ? original.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER) : original;
        }
        return null;
    }
}
