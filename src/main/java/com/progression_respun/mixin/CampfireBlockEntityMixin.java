package com.progression_respun.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.util.PlayerUtil.applyEffects;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void progressionrespun$applyEffect(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        int radius = 16;
        if (state.get(Properties.SIGNAL_FIRE)) {
            radius = radius + radius/2;
        }
        applyEffects(world, pos, radius);
    }
}
