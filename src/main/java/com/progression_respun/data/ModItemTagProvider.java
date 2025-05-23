package com.progression_respun.data;

import com.progression_respun.compat.EnderscapeItems;
import com.progression_respun.compat.GalosphereItems;
import com.progression_respun.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

import static com.progression_respun.compat.CompatMods.ENDERSCAPE;
import static com.progression_respun.compat.CompatMods.GALOSPHERE;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(
                        ModItems.FLINT_SWORD,
                        ModItems.COPPER_SWORD
                );
        getOrCreateTagBuilder(ItemTags.AXES)
                .add(
                        ModItems.FLINT_AXE,
                        ModItems.COPPER_AXE
                );
        getOrCreateTagBuilder(ItemTags.PICKAXES)
                .add(
                        ModItems.FLINT_PICKAXE,
                        ModItems.COPPER_PICKAXE
                );
        getOrCreateTagBuilder(ConventionalItemTags.RAW_MATERIALS)
                .add(
                        ModItems.RAW_COPPER_BAR,
                        ModItems.RAW_IRON_BAR,
                        ModItems.RAW_GOLD_BAR
                );
        if (GALOSPHERE) {
            getOrCreateTagBuilder(ConventionalItemTags.RAW_MATERIALS)
                    .add(
                            GalosphereItems.RAW_SILVER_BAR
                    );
        }
        if (ENDERSCAPE) {
            getOrCreateTagBuilder(ConventionalItemTags.RAW_MATERIALS)
                    .add(
                            EnderscapeItems.RAW_SHADOLINE_BAR
                    );
        }
    }
}