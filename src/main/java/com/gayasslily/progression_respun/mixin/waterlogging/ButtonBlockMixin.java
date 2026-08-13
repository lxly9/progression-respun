package com.gayasslily.progression_respun.mixin.waterlogging;

import com.gayasslily.progression_respun.mixin.BlockAccessor;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(ButtonBlock.class)
public class ButtonBlockMixin extends Block implements Waterloggable {
    public ButtonBlockMixin(Settings settings) {
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
    private void progressionrespun$appendSnippedProperty(BlockSetType blockSetType, int pressTicks, Settings settings, CallbackInfo ci) {
        Block buttonBlock = ButtonBlock.class.cast(this);
        BlockState defaultBlockState = buttonBlock.getDefaultState();
        ((BlockAccessor) buttonBlock).invokeSetDefaultState(defaultBlockState.with(WATERLOGGED, false));
    }
}
