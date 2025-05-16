package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState litWhenPlaced(BlockState state) {
        return state.with(CampfireBlock.LIT, false);
    }
}
