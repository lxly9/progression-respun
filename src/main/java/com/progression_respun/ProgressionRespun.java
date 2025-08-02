package com.progression_respun;

import com.progression_respun.block.ModBlocks;
import com.progression_respun.block.entity.ModBlockEntities;
import com.progression_respun.compat.CompatMods;
import com.progression_respun.item.ModItems;
import com.progression_respun.recipe.ModRecipes;
import com.progression_respun.worldgen.ModFeatures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

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
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}
}