package com.progression_respun.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public record GrindingRecipe(Ingredient inputItem, ItemStack output, ItemStack shard, int maxShardCount, float experience, float chance) implements Recipe<GrindingRecipeInput> {

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();
        defaultedList.add(this.inputItem);
        return defaultedList;
    }

    @Override
    public boolean matches(GrindingRecipeInput input, World world) {
        if(world.isClient) {
            return false;
        }
        return inputItem.test(input.getStackInSlot(0));
    }

    @Override
    public ItemStack craft(GrindingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
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

    public float getChance() {
        return chance;
    }

    public ItemStack getShard() {
        return shard;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GRINDING_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.GRINDING_RECIPE_TYPE;
    }

    public static class Serializer implements RecipeSerializer<GrindingRecipe> {

        public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(GrindingRecipe::inputItem),
                Codec.list(ResultEntry.CODEC.codec()).fieldOf("result").forGetter(recipe -> {
                    List<ResultEntry> list = new ArrayList<>();
                    list.add(new ResultEntry(recipe.output(), 1.0f, 0, recipe.experience()));
                    if (!recipe.shard().isEmpty()) {
                        list.add(new ResultEntry(recipe.shard(), recipe.chance(), recipe.maxShardCount(), 0.0f));
                    }
                    return list;
                })
        ).apply(instance, (input, results) -> {
            ItemStack resultStack = ItemStack.EMPTY;
            ItemStack shardStack = ItemStack.EMPTY;
            float chance = 0.0f;
            int shardMaxCount = 0;
            float experience = 0.0f;

            for (ResultEntry entry : results) {
                if (entry.chance() >= 1.0f) {
                    resultStack = entry.stack();
                    experience = entry.experience();
                } else {
                    shardStack = entry.stack();
                    chance = entry.chance();
                    shardMaxCount = entry.shardMaxCount();
                }
            }

            return new GrindingRecipe(input, resultStack, shardStack, shardMaxCount, experience, chance);
        }));


        public static final PacketCodec<RegistryByteBuf, GrindingRecipe> STREAM_CODEC =
                PacketCodec.tuple(
                        Ingredient.PACKET_CODEC, GrindingRecipe::inputItem,
                        ItemStack.PACKET_CODEC, GrindingRecipe::output,
                        ItemStack.PACKET_CODEC, GrindingRecipe::shard,
                        PacketCodecs.VAR_INT, GrindingRecipe::maxShardCount,
                        PacketCodecs.FLOAT, GrindingRecipe::experience,
                        PacketCodecs.FLOAT, GrindingRecipe::chance,
                        GrindingRecipe::new
                );

        @Override
        public MapCodec<GrindingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, GrindingRecipe> packetCodec() {
            return STREAM_CODEC;
        }

        public record ResultEntry(ItemStack stack, float chance, int shardMaxCount, float experience) {

            public ResultEntry(ItemStack stack, float chance, int shardMaxCount) {
                this(stack, chance, shardMaxCount, 0.0f);
            }

            public static final MapCodec<ResultEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemStack.CODEC.fieldOf("item").forGetter(ResultEntry::stack),
                    Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(ResultEntry::chance),
                    Codec.INT.optionalFieldOf("shardMaxCount", 0).forGetter(ResultEntry::shardMaxCount),
                    Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(ResultEntry::experience)
            ).apply(inst, ResultEntry::new));
        }
    }
}
