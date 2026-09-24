package com.gayasslily.progression_respun.mixin.oxidizable;

import com.gayasslily.progression_respun.ProgressionRespun;
import com.gayasslily.progression_respun.block.OxidizableObserverBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.ObserverBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {
    @WrapOperation(method = {"scheduledTick", "scheduleTick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    private void progressionrespun$scheduleDelay(ServerWorld instance, BlockPos blockPos, Block block, int i, Operation<Void> original) {
        if ((Object)this instanceof OxidizableObserverBlock observerBlock) original.call(instance, blockPos, block, ProgressionRespun.getDelayForOxidization(observerBlock.getDegradationLevel()));
    }
}
