package com.progression_respun.data;

import com.progression_respun.compat.EnderscapeItems;
import com.progression_respun.compat.GalosphereItems;
import com.progression_respun.compat.VanillaItems;
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

    public static final TagKey<Item> POLISHABLE_GEM = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "polishable_gems"));
    public static final TagKey<Item> BYPASSES_UNDER_ARMOR = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "bypasses_under_armor"));
    public static final TagKey<Item> UNDER_ARMOR = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "under_armor"));
    public static final TagKey<Item> CAN_BURN_COBWEBS = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "can_burn_cobwebs"));
    public static final TagKey<Item> CAN_FERTILIZE_FARMLAND = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "can_fertilize_farmland"));
    public static final TagKey<Item> BAIT = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "bait"));
    public static final TagKey<Item> TIME_REDUCTION_BAIT = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "fishing_time_reduction_bait"));
    public static final TagKey<Item> LUCK_BAIT = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "luck_bait"));
    public static final TagKey<Item> ENTITY_BAIT = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "entity_bait"));
    public static final TagKey<Item> MENDING_ENCHANTABLE = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "mending_enchantable"));
    public static final TagKey<Item> HORSE_ARMOR = TagKey.of(RegistryKeys.ITEM, Identifier.of("progression_respun", "horse_armor"));

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

        getOrCreateTagBuilder(UNDER_ARMOR)
                .add(
                        Items.LEATHER_HELMET,
                        Items.CHAINMAIL_HELMET,
                        Items.LEATHER_CHESTPLATE,
                        Items.CHAINMAIL_CHESTPLATE,
                        Items.LEATHER_LEGGINGS,
                        Items.CHAINMAIL_LEGGINGS ,
                        Items.LEATHER_BOOTS,
                        Items.CHAINMAIL_BOOTS
                );

        getOrCreateTagBuilder(POLISHABLE_GEM)
                .add(
                        Items.DIAMOND
                );

        getOrCreateTagBuilder(CAN_BURN_COBWEBS)
                .add(
                        Items.FLINT_AND_STEEL,
                        Items.TORCH,
                        Items.SOUL_TORCH,
                        ModItems.FIRESTARTER
                );

        getOrCreateTagBuilder(CAN_FERTILIZE_FARMLAND)
                .add(
                        ModItems.WORM
                );

        getOrCreateTagBuilder(TIME_REDUCTION_BAIT)
                .add(
                        ModItems.WORM
                );

        getOrCreateTagBuilder(ENTITY_BAIT)
                .add(
                        Items.CARROT,
                        Items.WARPED_FUNGUS
                );

        getOrCreateTagBuilder(BAIT)
                .add(
                        ModItems.WORM,
                        Items.CARROT,
                        Items.WARPED_FUNGUS
                );

        getOrCreateTagBuilder(ConventionalItemTags.NUGGETS)
                .add(
                        VanillaItems.COPPER_NUGGET
                );

        getOrCreateTagBuilder(MENDING_ENCHANTABLE)
                .forceAddTag(ItemTags.HEAD_ARMOR)
                .forceAddTag(ItemTags.CHEST_ARMOR)
                .forceAddTag(ItemTags.LEG_ARMOR)
                .forceAddTag(ItemTags.FOOT_ARMOR)
                .forceAddTag(ItemTags.PICKAXES)
                .forceAddTag(ItemTags.SWORDS)
                .forceAddTag(ItemTags.SHOVELS)
                .forceAddTag(ItemTags.AXES)
                .forceAddTag(ItemTags.HOES);

        getOrCreateTagBuilder(HORSE_ARMOR)
                .add(Items.LEATHER_HORSE_ARMOR)
                .add(ModItems.CHAINMAIL_HORSE_ARMOR)
                .add(Items.IRON_HORSE_ARMOR)
                .add(Items.GOLDEN_HORSE_ARMOR)
                .add(Items.DIAMOND_HORSE_ARMOR)
                .add(ModItems.NETHERITE_HORSE_ARMOR);

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