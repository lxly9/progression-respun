package com.gayasslily.progression_respun.mixin.oxidizable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static com.gayasslily.progression_respun.ProgressionRespun.progressionrespun$getOxidizedPiston;
import static com.gayasslily.progression_respun.ProgressionRespun.progressionrespun$getWaxedOxidizedPiston;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin {

    @Shadow
    private BlockState pushedBlock;

    @WrapOperation(method = "getHeadBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"))
    private BlockState progressionrespun$getOxidizedPistonState(Block instance, Operation<BlockState> original) {
        Block block = instance instanceof Oxidizable ? progressionrespun$getOxidizedPiston(instance, this.pushedBlock.getBlock()) : progressionrespun$getWaxedOxidizedPiston(instance, this.pushedBlock.getBlock());
        return original.call(block);
    }

    @WrapOperation(method = "getHeadBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean progressionrespun$isOfOxidizedPistonState(BlockState instance, Block block, Operation<Boolean> original) {
        Block block1 = instance.getBlock() instanceof Oxidizable ? progressionrespun$getOxidizedPiston(block, instance.getBlock()) : progressionrespun$getWaxedOxidizedPiston(block, instance.getBlock());
        return original.call(instance, block1);
    }

    @WrapOperation(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean progressionrespun$ofOxidizedPistonState(BlockState instance, Block block, Operation<Boolean> original) {
        Block block1 = instance.getBlock() instanceof Oxidizable ? progressionrespun$getOxidizedPiston(block, instance.getBlock()) : progressionrespun$getWaxedOxidizedPiston(block, instance.getBlock());
        return original.call(instance, block1);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private static boolean progressionrespun$isOxidizedPistonState(BlockState instance, Block block, Operation<Boolean> original) {
        Block block1 = instance.getBlock() instanceof Oxidizable ? progressionrespun$getOxidizedPiston(block, instance.getBlock()) : progressionrespun$getWaxedOxidizedPiston(block, instance.getBlock());
        return original.call(instance, block1);
    }
}
