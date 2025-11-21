package com.progression_respun.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.collection.DefaultedList;

public class BrewingRecipeInput implements RecipeInput {

    private final DefaultedList<ItemStack> stacks;

    public BrewingRecipeInput(ItemStack bottle1, ItemStack ingredient) {
        this.stacks = DefaultedList.ofSize(4, ItemStack.EMPTY);
        this.stacks.set(0, bottle1);
        this.stacks.set(1, ingredient);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public int getSize() {
        return stacks.size();
    }
}
