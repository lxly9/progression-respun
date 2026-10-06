package com.gayasslily.progression_respun.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.PistonType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import static com.gayasslily.progression_respun.ProgressionRespun.progressionrespun$getWaxedOxidizedPiston;

public class WaxedPistonHeadBlock extends PistonHeadBlock {

    public WaxedPistonHeadBlock(Settings settings) {
        super(settings);
    }

    private boolean isAttached(BlockState headState, BlockState pistonState) {
        Block block = headState.get(TYPE) == PistonType.DEFAULT ? progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON, this) : progressionrespun$getWaxedOxidizedPiston(Blocks.STICKY_PISTON, this);
        return pistonState.isOf(block) && pistonState.get(PistonBlock.EXTENDED) != false && pistonState.get(FACING) == headState.get(FACING);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos blockPos;
        if (!world.isClient && player.getAbilities().creativeMode && this.isAttached(state, world.getBlockState(blockPos = pos.offset(state.get(FACING).getOpposite())))) {
            world.breakBlock(blockPos, false);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        super.onStateReplaced(state, world, pos, newState, moved);
        BlockPos blockPos = pos.offset(state.get(FACING).getOpposite());
        if (this.isAttached(state, world.getBlockState(blockPos))) {
            world.breakBlock(blockPos, true);
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.offset(state.get(FACING).getOpposite()));
        return this.isAttached(state, blockState) || blockState.isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.MOVING_PISTON, this)) && blockState.get(FACING) == state.get(FACING);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(state.get(TYPE) == PistonType.STICKY ? progressionrespun$getWaxedOxidizedPiston(Blocks.STICKY_PISTON, this) : progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON, this));
    }
}
