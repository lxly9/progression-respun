package com.gayasslily.progression_respun.entity.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import static com.gayasslily.progression_respun.ProgressionRespun.MOD_ID;

public class ModEntityAttributes {

    public static final RegistryEntry<EntityAttribute> GENERIC_WEIGHT;

    private static RegistryEntry<EntityAttribute> register(String id, EntityAttribute attribute) {
        return Registry.registerReference(Registries.ATTRIBUTE, Identifier.of(MOD_ID, id), attribute);
    }

    static {
        GENERIC_WEIGHT = register("generic.weight", (new ClampedEntityAttribute("attribute.name.generic.weight", 0.7, 0.0F, 1024.0F)).setTracked(true));
    }

    public static void initialize() {}
}
