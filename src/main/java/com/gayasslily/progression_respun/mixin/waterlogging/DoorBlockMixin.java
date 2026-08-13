package com.gayasslily.progression_respun.mixin.waterlogging;

import com.gayasslily.progression_respun.mixin.BlockAccessor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(DoorBlock.class)
public class DoorBlockMixin extends Block implements Waterloggable {

    public DoorBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "appendProperties", at = @At("RETURN"))
    private void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Properties.WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void progressionrespun$appendSnippedProperty(BlockSetType type, Settings settings, CallbackInfo ci) {
        Block doorBlock = DoorBlock.class.cast(this);
        BlockState defaultBlockState = doorBlock.getDefaultState();
        ((BlockAccessor) doorBlock).invokeSetDefaultState(defaultBlockState.with(WATERLOGGED, false));
    }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState gay(BlockState original, ItemPlacementContext context) {
        return original.with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
    }
}
