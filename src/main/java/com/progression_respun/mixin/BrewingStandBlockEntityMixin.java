//package com.progression_respun.mixin;
//
//import com.progression_respun.recipe.BrewingRecipe;
//import com.progression_respun.recipe.BrewingRecipeInput;
//import com.progression_respun.recipe.ModRecipes;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.BrewingStandBlock;
//import net.minecraft.block.entity.BlockEntity;
//import net.minecraft.block.entity.BlockEntityType;
//import net.minecraft.block.entity.BrewingStandBlockEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.recipe.RecipeEntry;
//import net.minecraft.registry.RegistryWrapper;
//import net.minecraft.util.collection.DefaultedList;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.Arrays;
//import java.util.Optional;
//
//import static com.progression_respun.ProgressionRespun.PR_LOGGER;
//
//@Mixin(BrewingStandBlockEntity.class)
//public abstract class BrewingStandBlockEntityMixin extends BlockEntity {
//
//
//    public BrewingStandBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
//        super(type, pos, state);
//    }
//
//    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
//    private static void jsonDrivenBrewing(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
//        if (world.isClient) return;
//        BrewingStandBlockEntityAccessor accessor = (BrewingStandBlockEntityAccessor) blockEntity;
//
//        DefaultedList<ItemStack> inv = accessor.getInventory();
//        ItemStack ingredient = inv.get(3);
//        ItemStack itemStack = inv.get(4);
//
//        if (accessor.getFuel() <= 0 && itemStack.isOf(Items.BLAZE_POWDER)) {
//            accessor.setFuel(20);
//            itemStack.decrement(1);
//            markDirty(world, pos, state);
//        }
//
//        if (!ingredient.isEmpty()) {
//
//            boolean canStart = accessor.getFuel() > 0;
//
//            ItemStack bottle = inv.get(0);
//            if (!bottle.isEmpty() && hasRecipe(world, bottle, ingredient)) {
//                PR_LOGGER.info("gay");
//            }
//
//            ci.cancel();
//        }
//        ci.cancel();
//    }
//
//    @Unique
//    private static boolean hasRecipe(World world, ItemStack bottle, ItemStack ingredient) {
//        if (!bottle.isEmpty() && !ingredient.isEmpty()) {
//            PR_LOGGER.info("{}{}", String.valueOf(bottle), String.valueOf(ingredient));
//
//            BrewingRecipeInput input = new BrewingRecipeInput(bottle, ingredient);
//            Optional<RecipeEntry<BrewingRecipe>> match = world.getRecipeManager().getFirstMatch(ModRecipes.BREWING_RECIPE_TYPE, input, world);
//            return match.isPresent();
//        }
//        return false;
//    }
//}
