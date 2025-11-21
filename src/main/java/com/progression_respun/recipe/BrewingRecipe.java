package com.progression_respun.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public record BrewingRecipe(Ingredient inputItem, Ingredient inputPotion, ItemStack output, float experience) implements Recipe<BrewingRecipeInput> {

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();
        defaultedList.add(this.inputItem);
        return defaultedList;
    }

    @Override
    public boolean matches(BrewingRecipeInput input, World world) {
        if(world.isClient) {
            return false;
        }
        return inputItem.test(input.getStackInSlot(0));
    }

    @Override
    public ItemStack craft(BrewingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BREWING_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BREWING_RECIPE_TYPE;
    }

    public static class Serializer implements RecipeSerializer<BrewingRecipe> {

        public static final MapCodec<BrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(BrewingRecipe::inputItem),
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input_potion").forGetter(BrewingRecipe::inputPotion),
                Codec.list(ResultEntry.CODEC.codec()).fieldOf("result").forGetter(recipe -> {
                    List<ResultEntry> list = new ArrayList<>();
                    list.add(new ResultEntry(recipe.output(), recipe.experience()));
                    return list;
                })
        ).apply(instance, (input, inputPotion, results) -> {
            ItemStack resultStack = ItemStack.EMPTY;
            float experience = 0.0f;

            for (ResultEntry entry : results) {
                resultStack = entry.stack();
                experience = entry.experience();
            }
            return new BrewingRecipe(input, inputPotion, resultStack, experience);
        }));


        public static final PacketCodec<RegistryByteBuf, BrewingRecipe> STREAM_CODEC =
                PacketCodec.tuple(
                        Ingredient.PACKET_CODEC, BrewingRecipe::inputItem,
                        Ingredient.PACKET_CODEC, BrewingRecipe::inputPotion,
                        ItemStack.PACKET_CODEC, BrewingRecipe::output,
                        PacketCodecs.FLOAT, BrewingRecipe::experience,
                        BrewingRecipe::new
                );

        @Override
        public MapCodec<BrewingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, BrewingRecipe> packetCodec() {
            return STREAM_CODEC;
        }

        public record ResultEntry(ItemStack stack, float experience) {

            public ResultEntry(ItemStack stack) {
                this(stack, 0.0F);
            }

            public static final MapCodec<ResultEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemStack.CODEC.fieldOf("item").forGetter(ResultEntry::stack),
                    Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(ResultEntry::experience)
            ).apply(inst, ResultEntry::new));
        }
    }
}
