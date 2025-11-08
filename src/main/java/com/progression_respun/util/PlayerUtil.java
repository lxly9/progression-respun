package com.progression_respun.util;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

import static com.progression_respun.util.ArmorUtil.*;

public class PlayerUtil {


    private static final Identifier COMFORT_ID = Identifier.of("farmersdelight", "comfort");
    private static final RegistryEntry<StatusEffect> COMFORT_EFFECT = Registries.STATUS_EFFECT.getEntry(COMFORT_ID).orElse(null);

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

    public static void applyEffects(World world, BlockPos pos) {
        if (!world.isClient) {
            int radius = 0;
            Difficulty difficulty = world.getLevelProperties().getDifficulty();

            if (difficulty == Difficulty.PEACEFUL) return;
            if (difficulty == Difficulty.EASY) radius = 32;
            if (difficulty == Difficulty.NORMAL) radius = 16;
            if (difficulty == Difficulty.HARD) radius = 8;

            Box area = new Box(
                    pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                    pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
            );
            List<PlayerEntity> list = world.getEntitiesByClass(PlayerEntity.class, area, player -> !player.isCreative());
            List<HostileEntity> mobList = world.getEntitiesByClass(HostileEntity.class, area, hostileEntity -> true);
            List<TameableEntity> petList = world.getEntitiesByClass(TameableEntity.class, area, TameableEntity::isTamed);

            for (PlayerEntity playerEntity : list) {
                if (mobList.isEmpty()) {
                    playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, false, false, false));
                }
            }
            for (TameableEntity tameableEntity : petList) {
                if (mobList.isEmpty()) {
                    tameableEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, false, false, false));
                }
            }
        }
    }
}
