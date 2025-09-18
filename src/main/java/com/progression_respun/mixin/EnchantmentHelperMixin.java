package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.data.ModItemTagProvider;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyReturnValue(method = "canHaveEnchantments", at = @At("RETURN"))
    private static boolean underArmorCantHaveEnchantments(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return !stack.isIn(ModItemTagProvider.UNDER_ARMOR) && stack.isEnchantable();
    }
}
