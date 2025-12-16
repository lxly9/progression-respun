package com.progression_respun.mixin.farming;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.util.PropertyUtil.FERTILIZED;


@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow
    private BlockState defaultState;

    @Inject(method = "appendProperties", at = @At("HEAD"))
    private void progressionrespun$addProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        Block block = (Block)(Object)this;

        if (block instanceof FarmlandBlock) {
            builder.add(FERTILIZED);
        }
    }

    @WrapMethod(method = "getDefaultState")
    private BlockState progressionrespun$changeDefaultState(Operation<BlockState> original) {
        Block block = (Block)(Object)this;
        if (block instanceof FarmlandBlock) {
            return original.call().with(FERTILIZED, false);
        }
        return original.call();
    }

    @WrapMethod(method = "onPlaced")
    private void progressionrespun$changePlacedBlock(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, Operation<Void> original) {
        Block block = state.getBlock();
        if (block instanceof FarmlandBlock){
            if (!world.isClient) {
                world.setBlockState(pos, state.with(FERTILIZED, false));
            }
        }
    }
}




