package com.progression_respun.item;

import com.progression_respun.ProgressionRespun;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static com.progression_respun.compat.VanillaItems.COPPER_NUGGET;

public class ModItems {
    // Bars
    public static final Item FLINT_BAR = register(new Item(new Item.Settings()), "flint_bar");
    public static final Item POLISHED_DIAMOND = register(new Item(new Item.Settings()), "polished_diamond");
    public static final Item RAW_COPPER_BAR = register(new Item(new Item.Settings()), "raw_copper_bar");
    public static final Item RAW_IRON_BAR = register(new Item(new Item.Settings()), "raw_iron_bar");
    public static final Item RAW_GOLD_BAR = register(new Item(new Item.Settings()), "raw_gold_bar");
    public static final Item DIAMOND_SHARD = register(new Item(new Item.Settings()), "diamond_shard");
    public static final FlintAndSteelItem FIRESTARTER = (FlintAndSteelItem) register(new FlintAndSteelItem(new FlintAndSteelItem.Settings().maxDamage(16)), "firestarter");

    // Flint Tools
    public static final Item FLINT_SWORD = register(new SwordItem(ModToolMaterials.FLINT, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.FLINT, 3, -2.4f))), "flint_sword");
    public static final Item FLINT_PICKAXE = register(new PickaxeItem(ModToolMaterials.FLINT, new Item.Settings()
            .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.FLINT, 1.0f, -2.8f))), "flint_pickaxe");
    public static final Item FLINT_AXE = register(new AxeItem(ModToolMaterials.FLINT, new Item.Settings()
            .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.FLINT, 6.0f, -3.2f))), "flint_axe");

    // Copper Tools
    public static final Item COPPER_SWORD = register(new SwordItem(ModToolMaterials.COPPER, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.COPPER, 3, -2.4f))), "copper_sword");
    public static final Item COPPER_PICKAXE = register(new PickaxeItem(ModToolMaterials.COPPER, new Item.Settings()
            .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.COPPER, 1.0f, -2.8f))), "copper_pickaxe");
    public static final Item COPPER_AXE = register(new AxeItem(ModToolMaterials.COPPER, new Item.Settings()
            .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.COPPER, 7.0f, -3.2f))), "copper_axe");

    private static Item register(Item item, String name) {
        return Registry.register(Registries.ITEM, ProgressionRespun.id(name), item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(group -> {
                    group.addAfter(Items.FLINT, FLINT_BAR);
                    group.addBefore(Items.DIAMOND, DIAMOND_SHARD);
                    group.addAfter(Items.DIAMOND, POLISHED_DIAMOND);
                    group.addAfter(Items.GOLD_NUGGET, COPPER_NUGGET);
                    group.addAfter(Items.RAW_COPPER, RAW_COPPER_BAR);
                    group.addAfter(Items.RAW_IRON, RAW_IRON_BAR);
                    group.addAfter(Items.RAW_GOLD, RAW_GOLD_BAR);
                });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(group -> {
                    group.addAfter(Items.WOODEN_SWORD, FLINT_SWORD);
                    group.addAfter(Items.WOODEN_AXE, FLINT_AXE);
                    group.addAfter(Items.STONE_SWORD, COPPER_SWORD);
                    group.addAfter(Items.STONE_AXE, COPPER_AXE);
                });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(group -> {
                    group.addAfter(Items.WOODEN_HOE,
                            FLINT_PICKAXE,
                            FLINT_AXE);
                    group.addAfter(Items.STONE_HOE,
                            COPPER_PICKAXE,
                            COPPER_AXE);
                    group.addBefore(Items.FLINT_AND_STEEL, FIRESTARTER);
                });
    }
}
