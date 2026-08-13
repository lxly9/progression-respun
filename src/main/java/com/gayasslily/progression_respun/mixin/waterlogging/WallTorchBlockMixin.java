package com.gayasslily.progression_respun.mixin.waterlogging;

import com.gayasslily.progression_respun.mixin.BlockAccessor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.state.property.Properties.LIT;
import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(WallTorchBlock.class)
public class WallTorchBlockMixin extends Block implements Waterloggable {
    public WallTorchBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "appendProperties", at = @At("RETURN"))
    private void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WATERLOGGED);
        builder.add(LIT);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void progressionrespun$appendSnippedProperty(SimpleParticleType simpleParticleType, Settings settings, CallbackInfo ci) {
        Block torchBlock = WallTorchBlock.class.cast(this);
        BlockState defaultBlockState = torchBlock.getDefaultState();
        ((BlockAccessor) torchBlock).invokeSetDefaultState(defaultBlockState.with(WATERLOGGED, false).with(LIT, true));
    }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState progressionrespun$modifyPlacementState(BlockState original, ItemPlacementContext ctx) {
        return original != null ? original.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER).with(LIT, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() != Fluids.WATER) : null;
    }

    @Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$gay(BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!state.get(LIT)) ci.cancel();
    }
}
