package com.progression_respun.util;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    public static Map<EntityAttribute, Pair<EntityAttributeModifier, EntityAttributeModifier>> collectModifiersForSlot(ItemStack under, ItemStack outer, AttributeModifierSlot slot) {
        Map<EntityAttribute, Pair<EntityAttributeModifier, EntityAttributeModifier>> map = new LinkedHashMap<>();

        under.applyAttributeModifier(slot, (attr, mod) -> {
            map.compute(attr.value(), (entityAttribute, entityAttributeModifierPair) -> {
                if (entityAttributeModifierPair == null) return new Pair<>(mod, null);
                return new Pair<>(mod, entityAttributeModifierPair.getRight());
            });
        });

        if (outer != null) {
            outer.applyAttributeModifier(slot, (attr, mod) -> {
                map.compute(attr.value(), (entityAttribute, entityAttributeModifierPair) -> {
                    if (entityAttributeModifierPair == null) return new Pair<>(null, mod);
                    return new Pair<>(entityAttributeModifierPair.getLeft(), mod);
                });
            });
        }

        return map;
    }

    public static void printAttributeLine(Consumer<Text> out, EntityAttribute attribute, @Nullable EntityAttributeModifier under, @Nullable EntityAttributeModifier outer) {
        Double underVal = under == null ? null : convertValue(attribute, under);
        Double outerVal = outer == null ? null : convertValue(attribute, outer);

        boolean hasUnder = underVal != null && Math.abs(underVal) > 1e-6;
        boolean hasOuter = outerVal != null && Math.abs(outerVal) > 1e-6;

        if (!hasUnder && !hasOuter) return;

        Text name = Text.translatable(attribute.getTranslationKey());
        Formatting formatting = attribute.getFormatting(true);

        if (!hasOuter && hasUnder) {
            out.accept(Text.literal("+" + AttributeModifiersComponent.DECIMAL_FORMAT.format(underVal)).append(" ").append(name).formatted(formatting));
            return;
        }

        if (!hasUnder && hasOuter) {
            out.accept(Text.literal("+" + AttributeModifiersComponent.DECIMAL_FORMAT.format(outerVal)).append(" ").append(name).formatted(formatting));
            return;
        }

        out.accept(Text.literal("+" + AttributeModifiersComponent.DECIMAL_FORMAT.format(outerVal)).append(" + " + AttributeModifiersComponent.DECIMAL_FORMAT.format(underVal)).append(" ").append(name).formatted(formatting));
    }


    private static double convertValue(EntityAttribute attribute, EntityAttributeModifier mod) {
        double value = mod.value();

        if (mod.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE ||
                mod.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            value *= 100.0;
        }

        if (attribute == EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE.value()) {
            value *= 10.0;
        }

        return value;
    }

}
