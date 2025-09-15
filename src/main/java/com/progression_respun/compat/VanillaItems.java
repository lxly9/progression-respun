package com.progression_respun.compat;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.progression_respun.ProgressionRespun.*;

public class VanillaItems {

    public static Identifier COPPER_NUGGET_ID = Identifier.of(MC_ID + ":copper_nugget");
    public static Item COPPER_NUGGET;

    public static void initialize() {
        COPPER_NUGGET = register(new Item(new Item.Settings()), COPPER_NUGGET_ID);
    }

    private static Item register(Item item, Identifier id) {
        return Registry.register(Registries.ITEM, id, item);
    }
}
