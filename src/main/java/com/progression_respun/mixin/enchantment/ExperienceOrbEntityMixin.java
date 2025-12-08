package com.progression_respun.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {

    @ModifyReturnValue(method = "repairPlayerGears", at = @At("RETURN"))
    private static int progressionrespun$noMoreMending(int original, @Local(name = "amount") int amount) {
        return amount;
    }
}
