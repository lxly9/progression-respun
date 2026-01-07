package com.progression_respun.util;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.FishingBaitContentsComponent;
import com.progression_respun.item.ModItems;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;


public class ComponentUtil {

    public static void registerComponents() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(Items.FISHING_ROD, builder -> {
                builder.add(ModDataComponentTypes.FISHING_BAIT, FishingBaitContentsComponent.DEFAULT);
            });
        });
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(ModItems.WORM, builder -> {
                builder.add(DataComponentTypes.MAX_DAMAGE, 8);
            });
        });
    }
}
