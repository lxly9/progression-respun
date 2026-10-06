package com.gayasslily.progression_respun.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.gayasslily.progression_respun.ProgressionRespun.progressionrespun$getOxidizedPiston;
import static com.gayasslily.progression_respun.ProgressionRespun.progressionrespun$getWaxedOxidizedPiston;

@Mixin(PistonBlockEntityRenderer.class)
public class PistonBlockEntityRendererMixin {

    @WrapOperation(method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", ordinal = 0))
    private boolean progressionrespun$ofOxidizedPistonState(BlockState instance, Block block, Operation<Boolean> original, @Local PistonBlockEntity entity) {
        Block block1 = instance.getBlock() instanceof Oxidizable ? progressionrespun$getOxidizedPiston(Blocks.PISTON_HEAD, entity.getPushedBlock().getBlock()) : progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON_HEAD, entity.getPushedBlock().getBlock());
        return original.call(instance, block1);
    }

    @WrapOperation(method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", ordinal = 1))
    private boolean progressionrespun$ofOxidizedStickyPistonState(BlockState instance, Block block, Operation<Boolean> original, @Local PistonBlockEntity entity) {
        Block block1 = instance.getBlock() instanceof Oxidizable ? progressionrespun$getOxidizedPiston(Blocks.STICKY_PISTON, entity.getPushedBlock().getBlock()) : progressionrespun$getWaxedOxidizedPiston(Blocks.STICKY_PISTON, entity.getPushedBlock().getBlock());
        return original.call(instance, block1);
    }

    @WrapOperation(method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"))
    private BlockState progressionrespun$getOxidizedPistonState(Block instance, Operation<BlockState> original, @Local PistonBlockEntity entity) {
        Block block1 = entity.getPushedBlock().getBlock();
        Block block = block1 instanceof Oxidizable ? progressionrespun$getOxidizedPiston(instance, block1) : progressionrespun$getWaxedOxidizedPiston(instance, block1);
        return original.call(block);
    }
}
