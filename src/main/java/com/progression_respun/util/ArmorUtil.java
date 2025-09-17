package com.progression_respun.util;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class ArmorUtil {

    public static void registerTrinketPredicates() {
        TrinketsApi.registerTrinketPredicate(
                Identifier.of("progression_respun", "equippable_if_not_broken"), (stack, slot, entity) -> {
                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        return TriState.FALSE;
                    } else {
                        return TriState.DEFAULT;
                    }
                }
        );
    }

    public static float calculatePredictedDamage(LivingEntity entity, DamageSource source, float incoming) {
        float afterArmor = source.isIn(DamageTypeTags.BYPASSES_ARMOR)
                ? incoming
                : DamageUtil.getDamageLeft(entity, incoming, source,
                entity.getArmor(),
                (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

        float protection = 0f;
        if (!source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                protection = EnchantmentHelper.getProtectionAmount(serverWorld, entity, source);
            }
        }

        float afterEnchants = DamageUtil.getInflictedDamage(afterArmor, protection);

        StatusEffectInstance res = entity.getStatusEffect(StatusEffects.RESISTANCE);
        if (res != null) {
            int amp = res.getAmplifier(); // 0 -> Resistance I
            float reduction = 0.2f * (amp + 1);
            float multiplier = Math.max(0f, 1f - reduction);
            afterEnchants = afterEnchants * multiplier;
        }

        return afterEnchants;
    }
}
