package com.progression_respun.data;

import com.progression_respun.block.ModBlocks;
import com.progression_respun.item.ModItems;
import com.progression_respun.recipe.CrucibleRecipeBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {

        //Crucible
        offerCrucibleRecipe(exporter, Items.RAW_COPPER, Items.COPPER_INGOT, 1);
        offerCrucibleRecipe(exporter, Items.RAW_GOLD, Items.GOLD_INGOT, 1);
        offerCrucibleRecipe(exporter, Items.RAW_IRON, Items.IRON_INGOT, 1);
        offerCrucibleRecipe(exporter, ModItems.RAW_COPPER_BAR, Items.COPPER_INGOT, 1);
        offerCrucibleRecipe(exporter, ModItems.RAW_GOLD_BAR, Items.GOLD_INGOT, 1);
        offerCrucibleRecipe(exporter, ModItems.RAW_IRON_BAR, Items.IRON_INGOT, 1);

        // Bars

        offerBarRecipe(exporter, Items.RAW_COPPER, ModItems.RAW_COPPER_BAR);
        offerBarRecipe(exporter, Items.RAW_IRON, ModItems.RAW_IRON_BAR);
        offerBarRecipe(exporter, Items.RAW_GOLD, ModItems.RAW_GOLD_BAR);

        //smelting

        offerSmelting(exporter, List.of(ModItems.RAW_COPPER_BAR), RecipeCategory.MISC, Items.COPPER_INGOT, 2.1f, 200, "copper_ingot");
        offerSmelting(exporter, List.of(ModItems.RAW_IRON_BAR), RecipeCategory.MISC, Items.IRON_INGOT, 2.1f, 200, "iron_ingot");
        offerSmelting(exporter, List.of(ModItems.RAW_GOLD_BAR), RecipeCategory.MISC, Items.GOLD_INGOT, 3.0f, 200, "gold_ingot");
        offerBlasting(exporter, List.of(ModItems.RAW_COPPER_BAR), RecipeCategory.MISC, Items.COPPER_INGOT, 2.1f, 100, "copper_ingot");
        offerBlasting(exporter, List.of(ModItems.RAW_IRON_BAR), RecipeCategory.MISC, Items.IRON_INGOT, 2.1f, 100, "iron_ingot");
        offerBlasting(exporter, List.of(ModItems.RAW_GOLD_BAR), RecipeCategory.MISC, Items.GOLD_INGOT, 3.0f, 100, "gold_ingot");


        //shapeless
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.FIRESTARTER)
                .input(Items.FLINT,2)
                .criterion(FabricRecipeProvider.hasItem(ModItems.FIRESTARTER), FabricRecipeProvider.conditionsFromItem(ModItems.FIRESTARTER))
                .offerTo(exporter);

        //shaped

        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.CRUCIBLE_BLOCK)
                .input('#', Items.STICK)
                .input('X', Items.RAW_COPPER)
                .pattern("X X")
                .pattern("X X")
                .pattern("#X#")
                .criterion(hasItem(ModBlocks.CRUCIBLE_BLOCK), conditionsFromItem(ModBlocks.CRUCIBLE_BLOCK))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, Blocks.CAMPFIRE)
                .input('#', Items.STICK)
                .input('X', Items.RAW_COPPER)
                .pattern(" X ")
                .pattern("X#X")
                .criterion(hasItem(ModBlocks.CRUCIBLE_BLOCK), conditionsFromItem(ModBlocks.CRUCIBLE_BLOCK))
                .offerTo(exporter);

        // Flint Tools

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.FLINT_SWORD)
                .input('#', Items.STICK)
                .input('X', ModItems.FLINT_BAR)
                .pattern("X")
                .pattern("X")
                .pattern("#")
                .criterion(hasItem(ModItems.FLINT_BAR), conditionsFromItem(ModItems.FLINT_BAR))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.FLINT_PICKAXE)
                .input('#', Items.STICK)
                .input('X', ModItems.FLINT_BAR)
                .pattern("XXX")
                .pattern(" # ")
                .pattern(" # ")
                .criterion(hasItem(ModItems.FLINT_BAR), conditionsFromItem(ModItems.FLINT_BAR))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.FLINT_AXE)
                .input('#', Items.STICK)
                .input('X', ModItems.FLINT_BAR)
                .pattern("XX")
                .pattern("X#")
                .pattern(" #")
                .criterion(hasItem(ModItems.FLINT_BAR), conditionsFromItem(ModItems.FLINT_BAR))
                .offerTo(exporter);

        // Copper Tools

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.COPPER_SWORD)
                .input('#', Items.STICK)
                .input('X', Items.COPPER_INGOT)
                .pattern("X")
                .pattern("X")
                .pattern("#")
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_PICKAXE)
                .input('#', Items.STICK)
                .input('X', Items.COPPER_INGOT)
                .pattern("XXX")
                .pattern(" # ")
                .pattern(" # ")
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_AXE)
                .input('#', Items.STICK)
                .input('X', Items.COPPER_INGOT)
                .pattern("XX")
                .pattern("X#")
                .pattern(" #")
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);
    }

    private void offerBarRecipe(RecipeExporter exporter, Item material, Item bar) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, bar)
                .input('X', material)
                .pattern("XXX")
                .pattern("XXX")
                .criterion(hasItem(material), conditionsFromItem(material))
                .offerTo(exporter);
    }

    private void offerCrucibleRecipe(RecipeExporter exporter, Item input, Item output, int count) {
        CrucibleRecipeBuilder.create(input.asItem(), output, count)
                .criterion(hasItem(input), conditionsFromItem(input))
                .offerTo(exporter);
    }
}
