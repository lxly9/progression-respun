package com.progression_respun.util;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.progression_respun.ProgressionRespun.MOD_ID;

public class PlayerUtil {

    public static void oneHitToOneHp() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity, damageSource, v) -> {
            if (v >= 20 && !damageSource.isIn(DamageTypeTags.IS_FALL)) {
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

    public static void registerResourcePacks() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer("progression_respun")
                .orElseThrow(() -> new IllegalStateException("Missing mod modContainer"));

        ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of(MOD_ID, "orngstone_copper"), modContainer,
                Text.translatable("pack.progression_respun.orngstone_copper"),
                ResourcePackActivationType.NORMAL
        );
        ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of(MOD_ID, "redstone_copper"), modContainer,
                Text.translatable("pack.progression_respun.redstone_copper"),
                ResourcePackActivationType.ALWAYS_ENABLED
        );
    }

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
}
