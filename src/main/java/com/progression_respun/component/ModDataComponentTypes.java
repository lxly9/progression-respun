package com.progression_respun.component;

import com.progression_respun.component.type.FishingBaitContentsComponent;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

import static com.progression_respun.ProgressionRespun.MOD_ID;

public class ModDataComponentTypes {

    public static final ComponentType<Integer> DAMAGE = register("damage", builder -> builder.codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT));
    public static final ComponentType<Integer> MAX_DAMAGE = register("max_damage", builder -> builder.codec(Codecs.POSITIVE_INT).packetCodec(PacketCodecs.VAR_INT));
    public static final ComponentType<UnderArmorContentsComponent> UNDER_ARMOR_CONTENTS =
            Registry.register(
                    Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(MOD_ID, "under_armor_contents"),
                    ComponentType.<UnderArmorContentsComponent>builder()
                            .codec(UnderArmorContentsComponent.CODEC)
                            .packetCodec(UnderArmorContentsComponent.PACKET_CODEC)
                            .build()
            );
    public static final ComponentType<FishingBaitContentsComponent> FISHING_BAIT =
            Registry.register(
                    Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(MOD_ID, "fishing_bait"),
                    ComponentType.<FishingBaitContentsComponent>builder()
                            .codec(FishingBaitContentsComponent.CODEC)
                            .packetCodec(FishingBaitContentsComponent.PACKET_CODEC)
                            .build()
            );

    private static <T>ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, name),
                builderOperator.apply(ComponentType.builder()).build());
    }
    public static void registerModDataComponentTypes() {
    }
}
