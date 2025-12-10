package com.progression_respun.util;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ConnectionParticle;

import static com.progression_respun.util.ParticleUtil.CURSE;

public class ClientParticleUtil {

    public static void registerParticle() {
        ParticleFactoryRegistry.getInstance().register(
                CURSE,
                ConnectionParticle.EnchantFactory::new
        );
    }
}
