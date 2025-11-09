package com.progression_respun.util;

import com.progression_respun.recipe.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RecipeUtil {

    public static void registerRecipeDisabler() {
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeUtil::applyRecipeFilter);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) applyRecipeFilter(server);
        });
    }

    public static void applyRecipeFilter(MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        Collection<RecipeEntry<?>> allRecipes = recipeManager.values();
        List<RecipeEntry<?>> recipesToKeep = new ArrayList<>(allRecipes.size());

        int removed = 0;
        for (RecipeEntry<?> entry : allRecipes) {
            Identifier identifier = entry.id();
            Recipe<?> recipe = entry.value();
            RecipeType<?> recipeType = recipe.getType();

            boolean isSmeltType =
                    recipeType.equals(RecipeType.SMELTING);

            boolean idHasOre = identifier.getPath().matches(".*(ore|copper|iron|gold|silver|netherite).*");

            if (isSmeltType && (idHasOre || identifier.getPath().equals("quartz"))) {
                removed++;
                continue;
            }

            recipesToKeep.add(entry);
        }

        recipeManager.setRecipes(recipesToKeep);
    }

    public static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float)multiplier * experience);
        float f = MathHelper.fractionalPart((float)multiplier * experience);
        if (f != 0.0f && Math.random() < (double)f) {
            ++i;
        }
        ExperienceOrbEntity.spawn(world, pos, i);
    }

    public static void grindingRecipe() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            Block block = world.getBlockState(pos).getBlock();

            if (block instanceof GrindstoneBlock) {
                if (world instanceof ServerWorld serverWorld && !player.isSneaking()) {
                    ItemStack inputStack = player.getStackInHand(hand);
                    Vec3d vec = pos.toCenterPos();

                    Optional<RecipeEntry<GrindingRecipe>> recipeOpt = world.getRecipeManager().getFirstMatch(ModRecipes.GRINDING_RECIPE_TYPE, new GrindingRecipeInput(inputStack), world);

                    if (recipeOpt.isPresent()) {
                        GrindingRecipe recipe = recipeOpt.get().value();
                        ItemStack result;
                        float roll = world.random.nextFloat();
                        var sound = SoundEvents.BLOCK_GRINDSTONE_USE;

                        if (roll < recipe.getChance()) {
                            result = recipe.getShard().copy();
                            if (recipe.maxShardCount() > 1) result.setCount(world.random.nextBetween(1, recipe.maxShardCount()));
                            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, inputStack.copy()), vec.x, vec.y + 0.3, vec.z, 15, 0.25, 0.25, 0.25, 0.05);
                            sound = SoundEvents.BLOCK_DECORATED_POT_SHATTER;
                        } else {
                            result = recipe.craft(new GrindingRecipeInput(inputStack), world.getRegistryManager());
                            dropExperience(serverWorld, vec, 1, recipe.experience());
                            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, inputStack.copy()), vec.x, vec.y + 0.3, vec.z, 10, 0.15, 0.15, 0.15, 0.01);
                        }

                        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1f, 1f);
                        player.getInventory().offerOrDrop(result);
                        inputStack.decrement(1);
                        player.setStackInHand(hand, inputStack);

                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });
    }
}
