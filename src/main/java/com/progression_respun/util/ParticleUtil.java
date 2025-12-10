package com.progression_respun.util;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.progression_respun.ProgressionRespun.MOD_ID;

public class ParticleUtil {
    public static final SimpleParticleType CURSE = Registry.register(
            Registries.PARTICLE_TYPE,
            Identifier.of(MOD_ID, "curse"),
            FabricParticleTypes.simple(true)
    );
}
