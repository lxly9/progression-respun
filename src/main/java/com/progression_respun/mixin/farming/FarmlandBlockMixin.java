package com.progression_respun.mixin.farming;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin extends Block {

    @Inject(method = "appendProperties", at = @At("HEAD"))
    public void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        super.appendProperties(builder);
    }

    public FarmlandBlockMixin(Settings settings) {
        super(settings);
    }
}
