package com.gayasslily.progression_respun.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.gayasslily.progression_respun.ProgressionRespun.MOD_ID;

public class ModPotionsTagsProvider extends FabricTagProvider<StatusEffect> {
    public ModPotionsTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, RegistryKeys.STATUS_EFFECT ,completableFuture);
    }

    public static final TagKey<StatusEffect> DISABLED_POTIONS = TagKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(MOD_ID , "disabled_status_effects"));

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(DISABLED_POTIONS)
                .add(StatusEffects.WATER_BREATHING.value());
    }
}
