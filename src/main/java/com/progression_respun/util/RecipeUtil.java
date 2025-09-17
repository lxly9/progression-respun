package com.progression_respun.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeUtil {

    public static void registerRecipeDisabler() {
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeUtil::applyRecipeFilter);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) applyRecipeFilter(server);
        });
    }

    public static void applyRecipeFilter(MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        Collection<RecipeEntry<?>> allRecipes = recipeManager.values();
        List<RecipeEntry<?>> recipesToKeep = new ArrayList<>(allRecipes.size());

        int removed = 0;
        for (RecipeEntry<?> entry : allRecipes) {
            Identifier identifier = entry.id();
            Recipe<?> recipe = entry.value();
            RecipeType<?> recipeType = recipe.getType();

            boolean isSmeltType =
                    recipeType.equals(RecipeType.SMELTING);

            boolean idHasOre = identifier.getPath().matches(".*(ore|copper|iron|gold|silver|netherite).*");

            if (isSmeltType && (idHasOre || identifier.getPath().equals("quartz"))) {
                removed++;
                continue;
            }

            recipesToKeep.add(entry);
        }

        recipeManager.setRecipes(recipesToKeep);
    }
}
