
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
import java.util.Objects;

public class CrucibleRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final Item result;
    private final Ingredient ingredient;
    private final int count;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    private final RecipeFactory<?> recipeFactory;

    public CrucibleRecipeBuilder(ItemConvertible ingredient, ItemConvertible output, int count, RecipeFactory<?> recipeFactory) {
        this.ingredient = Ingredient.ofItems(ingredient);
        this.result = output.asItem();
        this.count = count;
        this.recipeFactory = recipeFactory;
    }

    public static CrucibleRecipeBuilder create(Item input, ItemConvertible output, int count) {
        return new CrucibleRecipeBuilder(input, output, count, new RecipeFactory<>());
    }

    public CrucibleRecipeBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    @Override
    public CraftingRecipeJsonBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getOutputItem() {
        return result;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);

        Objects.requireNonNull(builder);
        CrucibleRecipe crucibleRecipe = this.recipeFactory.create(this.ingredient, new ItemStack(this.result, this.count), this.count);
        exporter.accept(recipeId, crucibleRecipe, builder.build(recipeId.withPrefixedPath("recipes/")));
    }

    @Override
    public void offerTo(RecipeExporter exporter) {
        this.offerTo(exporter, Identifier.of(
                Registries.ITEM.getId(getOutputItem()).getNamespace(),
                "crucible/" + Registries.ITEM.getId(getOutputItem()).getPath() + "_from_" +
                        Registries.ITEM.getId(this.ingredient.getMatchingStacks()[0].getItem()).getPath()
        ));
    }
}