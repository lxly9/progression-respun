package com.progression_respun.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.data.ModItemTagProvider;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantments.class)
public class EnchantmentsMixin {
//    @WrapOperation(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;definition(Lnet/minecraft/registry/entry/RegistryEntryList;IILnet/minecraft/enchantment/Enchantment$Cost;Lnet/minecraft/enchantment/Enchantment$Cost;I[Lnet/minecraft/component/type/AttributeModifierSlot;)Lnet/minecraft/enchantment/Enchantment$Definition;", ordinal = 18))
//    private static Enchantment.Definition gay(RegistryEntryList<Item> supportedItems, int weight, int maxLevel, Enchantment.Cost minCost, Enchantment.Cost maxCost, int anvilCost, AttributeModifierSlot[] slots, Operation<Enchantment.Definition> original, @Local(argsOnly = true) Registerable<Enchantment> registry) {
//        RegistryEntryLookup<Item> registryEntryLookup3 = registry.getRegistryLookup(RegistryKeys.ITEM);
//        return original.call(registryEntryLookup3.getOrThrow(ModItemTagProvider.MENDING_ENCHANTABLE), weight, maxLevel, minCost, maxCost, anvilCost, slots);
//    }
}
