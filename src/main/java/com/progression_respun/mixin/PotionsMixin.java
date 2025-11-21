package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Potions.class)
public class PotionsMixin {

//    @ModifyReturnValue(method = "register", at = @At("RETURN"))
//    private static RegistryEntry<Potion> blockPotions(RegistryEntry<Potion> original) {
//        String name = original.getIdAsString();
//        if (original == Potions.WATER_BREATHING) {
//            return Potions.WATER;
//        }
//        return original;
//    }
}
