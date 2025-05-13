package com.progression_respun.compat;

import com.progression_respun.ProgressionRespun;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class GalosphereItems {
    public static Identifier SILVER_BAR_ID = ProgressionRespun.id("galosphere/silver_bar");
    public static Item SILVER_BAR;

    public static Identifier RAW_SILVER_BAR_ID = ProgressionRespun.id("galosphere/raw_silver_bar");
    public static Item RAW_SILVER_BAR;

    public static void initialize() {
        SILVER_BAR = register(new Item(new Item.Settings()), SILVER_BAR_ID);
        RAW_SILVER_BAR = register(new Item(new Item.Settings()), RAW_SILVER_BAR_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(group -> {
                    group.add(SILVER_BAR);
                    group.add(RAW_SILVER_BAR);
                });
    }

    private static Item register(Item item, Identifier id) {
        return Registry.register(Registries.ITEM, id, item);
    }
}
