package com.gayasslily.progression_respun.mixin.waterlogging;

import com.gayasslily.progression_respun.mixin.BlockAccessor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(BedBlock.class)
public class BedBlockMixin extends Block implements Waterloggable {
    public BedBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "appendProperties", at = @At("RETURN"))
    private void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void progressionrespun$appendSnippedProperty(DyeColor color, Settings settings, CallbackInfo ci) {
        Block bedBlock = BedBlock.class.cast(this);
        BlockState defaultBlockState = bedBlock.getDefaultState();
        ((BlockAccessor) bedBlock).invokeSetDefaultState(defaultBlockState.with(WATERLOGGED, false));
    }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState progressionrespun$modifyPlacementState(BlockState original, ItemPlacementContext ctx) {
        return original != null ? original.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER) : null;
    }
}
