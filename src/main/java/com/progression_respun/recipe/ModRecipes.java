package com.progression_respun.recipe;

import com.progression_respun.ProgressionRespun;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {
    public static RecipeSerializer<CrucibleRecipe> CRUCIBLE_RECIPE_SERIALIZER;
    public static RecipeType<CrucibleRecipe> CRUCIBLE_RECIPE_TYPE;
    public static GrindingRecipe.Serializer GRINDING_RECIPE_SERIALIZER;
    public static RecipeType<GrindingRecipe> GRINDING_RECIPE_TYPE;

    public static void register() {
        CRUCIBLE_RECIPE_TYPE = Registry.register(
                Registries.RECIPE_TYPE,
                Identifier.of(ProgressionRespun.MOD_ID, "crucible"),
                new RecipeType<CrucibleRecipe>() {
                    @Override
                    public String toString() {
                        return "crucible";
                    }
                }
        );

        CRUCIBLE_RECIPE_SERIALIZER = Registry.register(
                Registries.RECIPE_SERIALIZER,
                Identifier.of(ProgressionRespun.MOD_ID, "crucible"),
                new CrucibleRecipe.Serializer()
        );

        GRINDING_RECIPE_TYPE = Registry.register(
                Registries.RECIPE_TYPE,
                Identifier.of(ProgressionRespun.MOD_ID, "grinding"),
                new RecipeType<GrindingRecipe>() {
                    @Override
                    public String toString() {
                        return "grinding";
                    }
                }
        );

        GRINDING_RECIPE_SERIALIZER = Registry.register(
                Registries.RECIPE_SERIALIZER,
                Identifier.of(ProgressionRespun.MOD_ID, "grinding"),
                new GrindingRecipe.Serializer()
        );
    }
}