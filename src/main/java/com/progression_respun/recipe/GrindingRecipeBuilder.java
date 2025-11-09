
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

public class GrindingRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final Item result;
    private final Item shard;
    private final Ingredient ingredient;
    private final float experience;
    private final float chance;
    private final int shardMaxCount;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    private final RecipeFactory<?> recipeFactory;

    public GrindingRecipeBuilder(ItemConvertible ingredient, ItemConvertible output, ItemConvertible shard, float experience, int shardMaxCount, float chance, RecipeFactory<?> recipeFactory) {
        this.ingredient = Ingredient.ofItems(ingredient);
        this.result = output.asItem();
        this.shard = shard.asItem();
        this.experience = experience;
        this.shardMaxCount = shardMaxCount;
        this.chance = chance;
        this.recipeFactory = recipeFactory;
    }

    public static GrindingRecipeBuilder create(Item input, ItemConvertible output, ItemConvertible shard, float experience, int shardMaxCount, float chance) {
        return new GrindingRecipeBuilder(input, output, shard, experience, shardMaxCount, chance, new RecipeFactory<>());
    }

    public GrindingRecipeBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    @Override
    public CraftingRecipeJsonBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getOutputItem() {
        if (Math.random() < chance) return shard;
        return result;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);

        GrindingRecipe grindingRecipe = this.recipeFactory.create(
                this.ingredient,
                new ItemStack(this.result),
                new ItemStack(this.shard),
                this.experience,
                this.shardMaxCount,
                this.chance
        );

        exporter.accept(recipeId, grindingRecipe, builder.build(recipeId.withPrefixedPath("recipes/")));
    }


    @Override
    public void offerTo(RecipeExporter exporter) {
        this.offerTo(exporter, Identifier.of(
                Registries.ITEM.getId(getOutputItem()).getNamespace(),
                "grinding/" + Registries.ITEM.getId(getOutputItem()).getPath() + "_from_" +
                        Registries.ITEM.getId(this.ingredient.getMatchingStacks()[0].getItem()).getPath()
        ));
    }
}