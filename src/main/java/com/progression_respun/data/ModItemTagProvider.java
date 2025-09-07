package com.progression_respun.data;

import com.progression_respun.compat.EnderscapeItems;
import com.progression_respun.compat.GalosphereItems;
import com.progression_respun.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.progression_respun.compat.CompatMods.ENDERSCAPE;
import static com.progression_respun.compat.CompatMods.GALOSPHERE;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    public static final TagKey<Item> BYPASSES_UNDER_ARMOR = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "bypasses_under_armor"));

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
        getOrCreateTagBuilder(BYPASSES_UNDER_ARMOR)
                .add(
                        Items.TURTLE_HELMET,
                        Items.CARVED_PUMPKIN,
                        Items.ELYTRA
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