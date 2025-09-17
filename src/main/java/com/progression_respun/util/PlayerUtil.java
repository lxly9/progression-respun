package com.progression_respun.util;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;

import static com.progression_respun.ProgressionRespun.LOGGER;
import static com.progression_respun.util.ArmorUtil.*;

public class PlayerUtil {

    public static void oneHitToOneHp() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity, damageSource, v) -> {
            String damageString = damageSource.getName();

            if (v >= 20 && !(damageSource.isIn(DamageTypeTags.IS_FALL) || damageString.equals("genericKill"))) {
                float predicted = calculatePredictedDamage(livingEntity, damageSource, v);
                float effectiveHealth = livingEntity.getHealth() + livingEntity.getAbsorptionAmount(); // absorption + current hp

                boolean willDie = predicted >= effectiveHealth;
                if (willDie) {
                    livingEntity.setHealth(1);
                    return false;
                }
            }
            return true;
        });
    }
}
