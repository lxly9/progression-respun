
package com.progression_respun.recipe;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrewingRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final Ingredient ingredient;
    private final Ingredient inputPotion;
    private final ItemStack result;
    private final float experience;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    private final RecipeFactory<?> recipeFactory;

    public BrewingRecipeBuilder(ItemConvertible ingredient, Ingredient inputPotion, ItemStack output, float experience, RecipeFactory<?> recipeFactory) {
        this.ingredient = Ingredient.ofItems(ingredient);
        this.inputPotion = inputPotion;
        this.result = output;
        this.experience = experience;
        this.recipeFactory = recipeFactory;
    }

    public static BrewingRecipeBuilder create(Item input, Ingredient inputPotion, ItemStack output, float experience) {
        return new BrewingRecipeBuilder(input, inputPotion, output, experience, new RecipeFactory<>());
    }

    public BrewingRecipeBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    @Override
    public CraftingRecipeJsonBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getOutputItem() {
        return result.getItem();
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);

        BrewingRecipe brewingRecipe = this.recipeFactory.create(
                this.ingredient,
                this.inputPotion,
                this.result.copy(),
                this.experience
        );

        exporter.accept(recipeId, brewingRecipe, builder.build(recipeId.withPrefixedPath("recipes/")));
    }


    @Override
    public void offerTo(RecipeExporter exporter) {
        this.offerTo(exporter, Identifier.of(
                Registries.ITEM.getId(getOutputItem()).getNamespace(),
                "brewing/" + Registries.ITEM.getId(getOutputItem()).getPath() + "_from_" +
                        Registries.ITEM.getId(this.ingredient.getMatchingStacks()[0].getItem()).getPath()
        ));
    }
}