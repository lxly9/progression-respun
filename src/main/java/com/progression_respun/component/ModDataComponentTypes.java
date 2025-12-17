package com.progression_respun.component;

import com.progression_respun.component.type.FishingBaitContentsComponent;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModDataComponentTypes {
    public static final ComponentType<UnderArmorContentsComponent> UNDER_ARMOR_CONTENTS =
            Registry.register(
                    Registries.DATA_COMPONENT_TYPE,
                    Identifier.of("progression_respun", "under_armor_contents"),
                    ComponentType.<UnderArmorContentsComponent>builder()
                            .codec(UnderArmorContentsComponent.CODEC)
                            .packetCodec(UnderArmorContentsComponent.PACKET_CODEC)
                            .build()
            );
    public static final ComponentType<FishingBaitContentsComponent> FISHING_BAIT =
            Registry.register(
                    Registries.DATA_COMPONENT_TYPE,
                    Identifier.of("progression_respun", "fishing_bait"),
                    ComponentType.<FishingBaitContentsComponent>builder()
                            .codec(FishingBaitContentsComponent.CODEC)
                            .packetCodec(FishingBaitContentsComponent.PACKET_CODEC)
                            .build()
            );

    public static void registerModDataComponentTypes() {
    }
}
