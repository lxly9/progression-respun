package com.progression_respun.util;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

import static com.progression_respun.compat.CompatMods.FARMERSDELIGHT;
import static com.progression_respun.util.ArmorUtil.*;

public class PlayerUtil {

    private static final Identifier SPEED_PENALTY = Identifier.of("progression_respun", "speed_penalty");

    public static void registerMobAttributes(ServerWorld world, PlayerEntity player) {

        EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);

        if (speed != null) {

            double baseSpeed = speed.getBaseValue();
            double modifier = 0;

            ItemStack stack = player.getEquippedStack();
            ItemStack armorstack = player.getEquippedStack();


            EntityAttributeModifier speedModifier = new EntityAttributeModifier(
                    SPEED_PENALTY,
                    modifier,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            speed.addPersistentModifier(speedModifier);
        }
    }

    public static void oneHitToOneHp() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity, damageSource, v) -> {
            String damageString = damageSource.getName();

            if (v >= 20 && !(damageSource.isIn(DamageTypeTags.IS_FALL) || damageString.equals("genericKill"))) {
                float predicted = calculatePredictedDamage(livingEntity, damageSource, v);
                float effectiveHealth = livingEntity.getHealth() + livingEntity.getAbsorptionAmount();

                boolean willDie = predicted >= effectiveHealth;
                if (willDie) {
                    livingEntity.setHealth(1);
                    return false;
                }
            }
            return true;
        });
    }

    public static void applyEffects(World world, BlockPos pos, int radius) {
        Identifier comfort_id = Identifier.of("farmersdelight", "comfort");
        RegistryEntry<StatusEffect> comfort_effect = Registries.STATUS_EFFECT.getEntry(comfort_id).orElse(null);

        if (!world.isClient) {
            var effect = StatusEffects.REGENERATION;
            Difficulty difficulty = world.getLevelProperties().getDifficulty();
            int mobRadius = radius;

            if (difficulty == Difficulty.EASY) {
                radius = radius - radius/4;
                mobRadius = radius;
            }
            if (difficulty == Difficulty.NORMAL) {
                radius = radius / 2;
                mobRadius = radius - radius/4;
            }
            if (difficulty == Difficulty.HARD) {
                radius = radius / 4;
                mobRadius = radius / 2;
            }

            Box litArea = new Box(
                    pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                    pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
            );
            Box mobArea = new Box(
                    pos.getX() - mobRadius, pos.getY() - mobRadius, pos.getZ() - mobRadius,
                    pos.getX() + mobRadius, pos.getY() + mobRadius, pos.getZ() + mobRadius
            );
            List<PlayerEntity> list = world.getEntitiesByClass(PlayerEntity.class, litArea, player -> !player.isCreative());
            List<HostileEntity> mobList = world.getEntitiesByClass(HostileEntity.class, mobArea, hostileEntity -> true);
            List<TameableEntity> petList = world.getEntitiesByClass(TameableEntity.class, litArea, TameableEntity::isTamed);

            if (mobList.isEmpty()) {
                if (FARMERSDELIGHT) effect = comfort_effect;

                for (PlayerEntity player : list) {
                    if (player.hasStatusEffect(effect)) {
                        if (Objects.requireNonNull(player.getStatusEffect(effect)).getDuration() <= 20) {
                            player.addStatusEffect(new StatusEffectInstance(effect, 200, 0, false, false, false));
                        }
                    } else {
                        player.addStatusEffect(new StatusEffectInstance(effect, 200, 0, false, false, false));
                    }
                }

                for (TameableEntity tameableEntity : petList) {
                    tameableEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0, false, true, false));
                }
            }
        }
    }
}
