package com.progression_respun.mixin;

import com.progression_respun.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneBlock.class)
public class GrindstoneBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void polishDiamonds(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getMainHandStack();
        int stackSize = 1;
        int totalXp = 0;
        if (stack.isOf(Items.DIAMOND)) {
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                if (player.isSneaking() && stack.getCount() > 1) {
                    stackSize = stack.getCount();
                }
                stack.decrement(stackSize);
                for (int i = 0; i < stackSize; i++) {
                    if (serverWorld.getRandom().nextFloat() < 0.25f) {
                        player.getInventory().offerOrDrop(new ItemStack(ModItems.DIAMOND_SHARD));
                    } else {
                        player.getInventory().offerOrDrop(new ItemStack(ModItems.POLISHED_DIAMOND));
                    }
                    totalXp += serverWorld.getRandom().nextBetween(1, 5);
                }
                ExperienceOrbEntity.spawn(serverWorld, hit.getPos(), totalXp);
                world.playSound(null, hit.getBlockPos(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS);
                cir.setReturnValue(ActionResult.SUCCESS);
                cir.cancel();
            } else {
                cir.setReturnValue(ActionResult.CONSUME);
                cir.cancel();
            }
        }
    }
}
