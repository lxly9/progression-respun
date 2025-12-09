package com.progression_respun.mixin.enchantment;

import com.progression_respun.block.ModBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.ProgressionRespun.POWER_PROVIDER_OFFSETS;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin{

    @Inject(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockWithEntity;randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", shift = At.Shift.AFTER), cancellable = true)
    private void gay(BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
        for (BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
            BlockPos realPos = pos.add(blockPos);
            BlockState shelf = world.getBlockState(realPos);

            if (random.nextInt(16) == 0 && shelf.isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER)) {
                world.addParticle(
                        ParticleTypes.ENCHANT,
                        pos.getX() + 0.5,
                        pos.getY() + 2.0,
                        pos.getZ() + 0.5,
                        blockPos.getX() + random.nextFloat() - 0.5,
                        blockPos.getY() - random.nextFloat() - 1.0F,
                        blockPos.getZ() + random.nextFloat() - 0.5
                );
            }

            if (random.nextInt(16) == 0 && shelf.isIn(ModBlockTags.INCREASES_CURSE)) {
                world.addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5,
                        pos.getY() + 2.0,
                        pos.getZ() + 0.5,
                        blockPos.getX() + random.nextFloat() - 0.5,
                        blockPos.getY() - random.nextFloat() - 1.0F,
                        blockPos.getZ() + random.nextFloat() - 0.5
                );
            }
        }
        ci.cancel();
    }
}
