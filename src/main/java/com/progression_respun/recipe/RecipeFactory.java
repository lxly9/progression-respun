package com.progression_respun.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public class RecipeFactory<T> {
    public CrucibleRecipe create(Ingredient ingredient, ItemStack itemStack, float experience, int count) {
        return new CrucibleRecipe(ingredient, new ItemStack(itemStack.getItem(), count), experience);
    }

    public GrindingRecipe create(Ingredient ingredient, ItemStack itemStack, ItemStack shard, float experience, int maxShardCount, float chance) {
        return new GrindingRecipe(ingredient, new ItemStack(itemStack.getItem()), new ItemStack(shard.getItem()), maxShardCount, experience, chance);
    }

    public BrewingRecipe create(Ingredient ingredient, Ingredient inputPotion, ItemStack itemStack, float experience) {
        return new BrewingRecipe(ingredient, inputPotion, new ItemStack(itemStack.getItem()), experience);
    }
}