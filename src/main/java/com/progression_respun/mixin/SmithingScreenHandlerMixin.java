package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

import static com.progression_respun.ProgressionRespun.getArmor;

@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {

    @WrapOperation(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/SmithingRecipe;craft(Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack progressionrespun$updateResult(SmithingRecipe instance, RecipeInput recipeInput, RegistryWrapper.WrapperLookup wrapperLookup, Operation<ItemStack> original, @Local RecipeEntry<SmithingRecipe> recipeEntry) {
        ItemStack stack = recipeInput.getStackInSlot(1).copy();
        ItemStack armorStack = getArmor(stack).copy();
        if (!armorStack.isEmpty()) {
            SmithingRecipeInput armorInput = new SmithingRecipeInput(recipeInput.getStackInSlot(0), armorStack, recipeInput.getStackInSlot(2));
            ItemStack armorStack2 = recipeEntry.value().craft(armorInput, wrapperLookup);
            stack.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, new UnderArmorContentsComponent(List.of(armorStack2)));
            return stack;
        }
        return original.call(instance, recipeInput, wrapperLookup);
    }

    @WrapOperation(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getAllMatches(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;)Ljava/util/List;"))
    private <I extends RecipeInput, T extends Recipe<I>> List<RecipeEntry<T>> progressionrespun$updateResult(RecipeManager instance, RecipeType<T> type, I input, World world, Operation<List<RecipeEntry<T>>> original) {
        ItemStack armorStack = getArmor(input.getStackInSlot(1));
        if (!armorStack.isEmpty()) {
            SmithingRecipeInput armorInput = new SmithingRecipeInput(input.getStackInSlot(0), armorStack, input.getStackInSlot(2));
            return original.call(instance, type, armorInput, world);
        }
        return original.call(instance, type, input, world);
    }
}
