package com.progression_respun.util;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class MobUtil {

    private static final Identifier SCALE_HEALTH_BY_HEIGHT = Identifier.of("progression_respun", "scale_health_by_height");
    private static final Identifier SCALE_DAMAGE_BY_HEIGHT = Identifier.of("progression_respun", "scale_damage_by_height");


    public static void registerMobAttributes(ServerWorld world, MobEntity mobEntity) {

        EntityAttributeInstance health = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        EntityAttributeInstance damage = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        if (mobEntity instanceof WardenEntity || mobEntity instanceof WitherEntity || mobEntity instanceof EnderDragonEntity) return;

        if (health != null && damage != null) {
            DimensionType dimensionType = world.getDimension();

            double baseHealth = health.getBaseValue();
            double modifier = 0;

            double mobPos = mobEntity.getY();
            BlockPos pos = mobEntity.getBlockPos().down();

            if (mobPos >= world.getSeaLevel()) {
                if (world.getBiome(pos).isIn(ConventionalBiomeTags.IS_CAVE)) {
                    modifier = 0.125;
                } else {
                    modifier = 0;
                }
            }
            if (mobPos <= world.getSeaLevel() && mobPos >= world.getSeaLevel() - world.getSeaLevel()  && !world.getBiome(pos).isIn(ConventionalBiomeTags.IS_AQUATIC)) {
                modifier = 0.25;
            }
            if (mobPos < world.getSeaLevel() - world.getSeaLevel()) {
                modifier = 0.75;
            }
            if (dimensionType.piglinSafe()) {
                modifier = 1;
            }

            EntityAttributeModifier healthModifier = new EntityAttributeModifier(
                    SCALE_HEALTH_BY_HEIGHT,
                    modifier,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            health.addPersistentModifier(healthModifier);

            EntityAttributeModifier damageModifier = new EntityAttributeModifier(
                    SCALE_DAMAGE_BY_HEIGHT,
                    modifier,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            damage.addPersistentModifier(damageModifier);

            mobEntity.heal((float) (baseHealth * modifier));
        }
    }

    public static void changeMobAttributes() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverWorld) -> {
            if (entity instanceof MobEntity mobEntity) {
                EntityAttributeInstance health = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                EntityAttributeInstance damage = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

                if (health != null && damage != null) {
                    boolean hasHealthMod = health.hasModifier(SCALE_HEALTH_BY_HEIGHT);
                    boolean hasDamageMod = damage.hasModifier(SCALE_DAMAGE_BY_HEIGHT);

                    if (!hasHealthMod && !hasDamageMod) {
                        registerMobAttributes(serverWorld, mobEntity);
                    }
                }
            }
        });
    }

    public static void despawnMobsOnWakeup() {
        EntitySleepEvents.STOP_SLEEPING.register((entity, blockPos) -> {
            if (entity instanceof PlayerEntity player) {
                World serverWorld = player.getWorld();
                long time = serverWorld.getTimeOfDay();
                Difficulty difficulty = serverWorld.getLevelProperties().getDifficulty();

                if (time == 24000) {
                    int radius = 0;

                    if (difficulty == Difficulty.PEACEFUL) return;
                    if (difficulty == Difficulty.EASY) radius = 96;
                    if (difficulty == Difficulty.NORMAL) radius = 64;
                    if (difficulty == Difficulty.HARD) radius = 32;

                    Box area = new Box(
                            blockPos.getX() - radius, blockPos.getY() - radius, blockPos.getZ() - radius,
                            blockPos.getX() + radius, blockPos.getY() + radius, blockPos.getZ() + radius
                    );

                    for (Entity entity1 : serverWorld.getOtherEntities(player, area)){
                        if (entity1 instanceof MobEntity mobEntity) {

                            BlockPos mobPos = mobEntity.getBlockPos();
                            int lightLevel = serverWorld.getChunkManager().getLightingProvider().get(LightType.SKY).getLightLevel(mobPos);

                            if (!(mobEntity.cannotDespawn() || mobEntity.isPersistent()) && lightLevel >= 5) {
                                mobEntity.discard();
                            }
                        }
                    }
                }
            }
        });
    }
}
