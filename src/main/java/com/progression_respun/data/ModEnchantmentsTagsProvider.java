package com.progression_respun.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentsTagsProvider extends FabricTagProvider.EnchantmentTagProvider {
    public ModEnchantmentsTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    public static final TagKey<Enchantment> DISABLED_ENCHANTMENTS = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("progression_respun", "disabled_enchantments"));

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(DISABLED_ENCHANTMENTS).add(
                Enchantments.PROTECTION,
                Enchantments.UNBREAKING,
                Enchantments.SHARPNESS,
                Enchantments.RESPIRATION,
                Enchantments.LUCK_OF_THE_SEA,
                Enchantments.LURE
        );
    }
}
