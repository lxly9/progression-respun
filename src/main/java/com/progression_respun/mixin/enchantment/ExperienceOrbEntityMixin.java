package com.progression_respun.mixin.enchantment;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {

    @Inject(method = "repairPlayerGears", at = @At("RETURN"), cancellable = true)
    private static void progressionrespun$noMoreMending(ServerPlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
        cir.cancel();
    }
}
