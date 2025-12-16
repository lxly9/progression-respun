package com.progression_respun.mixin.farming;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.block.ModBlocks;
import com.progression_respun.block.WitheredCropBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.progression_respun.ProgressionRespun.LOGGER;
import static com.progression_respun.ProgressionRespun.WITHERED_CROPS;
import static com.progression_respun.util.PropertyUtil.FERTILIZED;

@Debug(export = true)
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    @Shadow
    public abstract int getAge(BlockState state);

    @Shadow
    protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        return 0;
    }

    @Shadow
    public abstract BlockState withAge(int age);

    @Unique
    private float WITHER_CHANCE_MULTIPLIER = 1.0f;

    @Unique
    private float progressionrespun$getWitherChancePerAge(int age) {
        return switch (age) {
            case 2 -> 0.1f;
            case 3 -> 0.15f;
            case 4 -> 0.2f;
            case 5 -> 0.25f;
            case 6 -> 0.3f;
            case 7 -> 0.35f;
            default -> 0;
        };
    }

    @Unique
    private void getWitherChancePerWitheredCrop(World world, BlockPos pos) {
        WITHER_CHANCE_MULTIPLIER = 1.0f;
        for (BlockPos blockPos : WITHERED_CROPS) {
            if ((world.getBlockState(pos.add(blockPos)).getBlock() instanceof WitheredCropBlock)) {
                WITHER_CHANCE_MULTIPLIER = WITHER_CHANCE_MULTIPLIER + 0.5f;
            }
        }
    }

    @WrapMethod(method = "randomTick")
    private void progressionrespun$getMaxAgeWithering(BlockState state, ServerWorld world, BlockPos pos, Random random, Operation<Void> original) {
        BlockState farmland = world.getBlockState(pos.down());
        if (farmland.getBlock() instanceof FarmlandBlock && !farmland.get(FERTILIZED)) {
            if (world.getBaseLightLevel(pos, 0) >= 9) {
                int i = this.getAge(state);
                float f = getAvailableMoisture((Block) (Object) this, world, pos);
                if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                    getWitherChancePerWitheredCrop(world, pos);
                    LOGGER.info(String.valueOf(WITHER_CHANCE_MULTIPLIER));
                    if (progressionrespun$getWitherChancePerAge(i) > 0 && random.nextFloat() < progressionrespun$getWitherChancePerAge(i) * WITHER_CHANCE_MULTIPLIER) {
                        world.setBlockState(pos, ModBlocks.WITHERED_CROP.getDefaultState(), Block.NOTIFY_LISTENERS);
                    } else {
                        world.setBlockState(pos, this.withAge(i + 1), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        } else {
            original.call(state, world, pos, random);
        }
    }
}
