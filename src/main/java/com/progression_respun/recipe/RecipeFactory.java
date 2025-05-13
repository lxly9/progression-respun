package com.progression_respun.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public class RecipeFactory<T> {
    public CrucibleRecipe create(Ingredient ingredient, ItemStack itemStack, int count) {
        return new CrucibleRecipe(ingredient, new ItemStack(itemStack.getItem(), count));
    }
}