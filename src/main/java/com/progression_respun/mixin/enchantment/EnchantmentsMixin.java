package com.progression_respun.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.progression_respun.data.ModItemTagProvider;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(Enchantments.class)
public class EnchantmentsMixin {
    @WrapOperation(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryEntryLookup;getOrThrow(Lnet/minecraft/registry/tag/TagKey;)Lnet/minecraft/registry/entry/RegistryEntryList$Named;", ordinal = 63))
    private static <T> RegistryEntryList.Named<T> progressionrespun$mendingEnchantable(RegistryEntryLookup<T> instance, TagKey<T> tag, Operation<RegistryEntryList.Named<T>> original) {
        return original.call(instance, ModItemTagProvider.MENDING_ENCHANTABLE);
    }
}
