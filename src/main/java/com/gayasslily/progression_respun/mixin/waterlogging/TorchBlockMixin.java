package com.gayasslily.progression_respun.mixin.waterlogging;

import com.gayasslily.progression_respun.mixin.BlockAccessor;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.state.property.Properties.LIT;
import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(TorchBlock.class)
public class TorchBlockMixin extends Block implements Waterloggable {

    public TorchBlockMixin(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(LIT, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
        builder.add(LIT);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        if (ctx.getWorld() != null) {
            return this.getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER).with(LIT, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() != Fluids.WATER);
        }
        else return null;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void progressionrespun$appendSnippedProperty(SimpleParticleType simpleParticleType, Settings settings, CallbackInfo ci) {
        Block torchBlock = TorchBlock.class.cast(this);
        BlockState defaultBlockState = torchBlock.getDefaultState();
        ((BlockAccessor) torchBlock).invokeSetDefaultState(defaultBlockState.with(WATERLOGGED, false));
    }

    @Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$gay(BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!state.get(LIT)) ci.cancel();
    }
}
