package com.progression_respun.util;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import com.progression_respun.data.ModItemTagProvider;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

public class ArmorUtil {

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

    public static void registerComponent() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(
                    item -> item instanceof ArmorItem,
                    (builder, item) -> {
                        builder.add(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, new UnderArmorContentsComponent(new ArrayList<>()));
                    }
            );
        });
    }
}
