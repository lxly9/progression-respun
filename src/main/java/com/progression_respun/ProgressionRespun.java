package com.progression_respun;

import com.progression_respun.block.ModBlocks;
import com.progression_respun.block.entity.ModBlockEntities;
import com.progression_respun.compat.CompatMods;
import com.progression_respun.item.ModItems;
import com.progression_respun.recipe.ModRecipes;
import com.progression_respun.worldgen.ModFeatures;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ProgressionRespun implements ModInitializer {
	public static final String MOD_ID = "progression_respun";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.register();
		ModItems.initialize();
		ModFeatures.initialize();
		CompatMods.initialize();
		ModRecipes.register();
		ModBlocks.registerModBlocks();
		registerTrinketPredicates();
		changeMobAttributes();
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

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	private static final Identifier SCALE_HEALTH_BY_HEIGHT = Identifier.of("progression_respun", "scale_health_by_height");
	private static final Identifier SCALE_DAMAGE_BY_HEIGHT = Identifier.of("progression_respun", "scale_damage_by_height");

	public static void registerMobAttributes(ServerWorld world, MobEntity mobEntity) {

		EntityAttributeInstance health = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		EntityAttributeInstance damage = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		if (mobEntity instanceof WardenEntity || mobEntity instanceof WitherEntity || mobEntity instanceof EnderDragonEntity) return;

        if (health != null && damage != null) {
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
}