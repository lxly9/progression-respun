package com.progression_respun.mixin.under_armor_handling;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.ArmorMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorMaterial.class)
public class ArmorMaterialMixin {

    @ModifyReturnValue(method = "getProtection", at = @At("RETURN"))
    private int progressionrespun$nerfArmorToughness(int original) {
        int value = original;

        if (value == 2 || value == 3) value = value - 1;
        if (value >= 4) value = value - 2;

        return value;
    }
}
