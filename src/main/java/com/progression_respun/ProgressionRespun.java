package com.progression_respun;

import com.progression_respun.block.ModBlocks;
import com.progression_respun.block.entity.ModBlockEntities;
import com.progression_respun.compat.CompatMods;
import com.progression_respun.item.ModItems;
import com.progression_respun.recipe.ModRecipes;
import com.progression_respun.worldgen.ModFeatures;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
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

public class ProgressionRespun implements ModInitializer {
	public static final String MOD_ID = "progression_respun";

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

	public static void registerMobAttributes(ServerWorld world, BlockPos pos, MobEntity mobEntity) {
		EntityAttributeInstance health = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		EntityAttributeInstance damage = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		if (mobEntity instanceof WardenEntity || mobEntity instanceof WitherEntity || mobEntity instanceof EnderDragonEntity) return;

        if (health != null && damage != null) {
			double baseHealth = health.getBaseValue();
			double modifierDouble = 0;

			double mobPos = mobEntity.getY();

			if (mobPos >= world.getSeaLevel()) {
				if (world.getBiome(pos).isIn(ConventionalBiomeTags.IS_CAVE)) {
					modifierDouble = 0.125;
				} else {
					modifierDouble = 0;
				}
			}
			if (mobPos <= world.getSeaLevel() && mobPos >= world.getSeaLevel() - world.getSeaLevel()  && !world.getBiome(pos).isIn(ConventionalBiomeTags.IS_AQUATIC)) {
				modifierDouble = 0.25;
			}
			if (mobPos < world.getSeaLevel() - world.getSeaLevel()) {
				modifierDouble = 0.75;
			}

			EntityAttributeModifier healthModifier = new EntityAttributeModifier(
					Identifier.of("progression_respun", "scale_health_by_height"),
					modifierDouble,
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
			);
			health.addPersistentModifier(healthModifier);

			EntityAttributeModifier damageModifier = new EntityAttributeModifier(
					Identifier.of("progression_respun", "scale_damage_by_height"),
					modifierDouble,
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
			);
			damage.addPersistentModifier(damageModifier);

			mobEntity.heal((float) (baseHealth * modifierDouble));
		}
	}
}