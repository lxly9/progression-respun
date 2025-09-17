package com.progression_respun;

import com.progression_respun.block.ModBlocks;
import com.progression_respun.block.entity.ModBlockEntities;
import com.progression_respun.compat.CompatMods;
import com.progression_respun.item.ModItems;
import com.progression_respun.recipe.ModRecipes;
import com.progression_respun.worldgen.ModFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.progression_respun.util.ArmorUtil.*;
import static com.progression_respun.util.MobUtil.*;
import static com.progression_respun.util.PlayerUtil.*;


public class ProgressionRespun implements ModInitializer {
	public static final String MOD_ID = "progression_respun";
	public static final String MC_ID = "minecraft";
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
		despawnMobsOnWakeup();
		registerResourcePacks();
		oneHitToOneHp();
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
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
}