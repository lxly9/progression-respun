package com.progression_respun.mixin;

import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Potions.class)
public class PotionsMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void blockPotions(String name, Potion potion, CallbackInfoReturnable<RegistryEntry<Potion>> cir) {
        if (name.endsWith("_long")) {
            cir.cancel();
        }
    }
}
