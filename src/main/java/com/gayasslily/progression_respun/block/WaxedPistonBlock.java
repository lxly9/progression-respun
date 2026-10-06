package com.gayasslily.progression_respun.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gayasslily.progression_respun.ProgressionRespun.progressionrespun$getWaxedOxidizedPiston;

public class WaxedPistonBlock extends PistonBlock{
    private boolean sticky;

    //TODO Make all piston blocks oxidize at the same time

    public WaxedPistonBlock(boolean sticky, Settings settings) {
        super(sticky, settings);
        this.sticky = sticky;
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        Direction direction = state.get(FACING);
        BlockState blockState = state.with(EXTENDED, true);
        if (!world.isClient) {
            boolean bl = this.shouldExtend(world, pos, direction);
            if (bl && (type == 1 || type == 2)) {
                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
                return false;
            }
            if (!bl && type == 0) {
                return false;
            }
        }
        if (type == 0) {
            if (!this.move(world, pos, direction, true)) return false;
            world.setBlockState(pos, blockState, Block.NOTIFY_ALL | Block.MOVED);
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.25f + 0.6f);
            world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(blockState));
            return true;
        } else {
            if (type != 1 && type != 2) return true;
            BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));
            if (blockEntity instanceof PistonBlockEntity) {
                ((PistonBlockEntity) blockEntity).finish();
            }
            BlockState blockState2 = progressionrespun$getWaxedOxidizedPiston(Blocks.MOVING_PISTON, this).getDefaultState().with(PistonExtensionBlock.FACING, direction).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
            world.setBlockState(pos, blockState2, Block.FORCE_STATE | Block.NO_REDRAW);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(pos, blockState2, this.getDefaultState().with(FACING, Direction.byId(data & 7)), direction, false, true));
            world.updateNeighbors(pos, blockState2.getBlock());
            blockState2.updateNeighbors(world, pos, Block.NOTIFY_LISTENERS);
            if (sticky) {
                PistonBlockEntity pistonBlockEntity;
                BlockEntity blockEntity2;
                BlockPos blockPos = pos.add(direction.getOffsetX() * 2, direction.getOffsetY() * 2, direction.getOffsetZ() * 2);
                BlockState blockState3 = world.getBlockState(blockPos);
                boolean bl2 = false;
                if (blockState3.isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.MOVING_PISTON, this)) && (blockEntity2 = world.getBlockEntity(blockPos)) instanceof PistonBlockEntity && (pistonBlockEntity = (PistonBlockEntity) blockEntity2).getFacing() == direction && pistonBlockEntity.isExtending()) {
                    pistonBlockEntity.finish();
                    bl2 = true;
                }
                if (!bl2) {
                    if (type == 1 && !blockState3.isAir() && isMovable(blockState3, world, blockPos, direction.getOpposite(), false, direction) && (blockState3.getPistonBehavior() == PistonBehavior.NORMAL || blockState3.isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON, this)) || blockState3.isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.STICKY_PISTON, this)))) {
                        this.move(world, pos, direction, false);
                    } else {
                        world.removeBlock(pos.offset(direction), false);
                    }
                }
            } else {
                world.removeBlock(pos.offset(direction), false);
            }
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.15f + 0.6f);
            world.emitGameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Emitter.of(blockState2));
        }
        return true;
    }

    private boolean move(World world, BlockPos pos, Direction dir, boolean retract) {
        int k;
        BlockPos blockPos5;
        BlockPos blockPos3;
        int j;
        PistonHandler pistonHandler;
        BlockPos blockPos = pos.offset(dir);
        if (!retract && world.getBlockState(blockPos).isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON_HEAD, this))) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE | Block.NO_REDRAW);
        }
        if (!(pistonHandler = new PistonHandler(world, pos, dir, retract)).calculatePush()) {
            return false;
        }
        HashMap<BlockPos, BlockState> map = Maps.newHashMap();
        List<BlockPos> list = pistonHandler.getMovedBlocks();
        ArrayList<BlockState> list2 = Lists.newArrayList();
        for (BlockPos blockPos2 : list) {
            BlockState blockState = world.getBlockState(blockPos2);
            list2.add(blockState);
            map.put(blockPos2, blockState);
        }
        List<BlockPos> list3 = pistonHandler.getBrokenBlocks();
        BlockState[] blockStates = new BlockState[list.size() + list3.size()];
        Direction direction = retract ? dir : dir.getOpposite();
        int i = 0;
        for (j = list3.size() - 1; j >= 0; --j) {
            blockPos3 = list3.get(j);
            BlockState blockState = world.getBlockState(blockPos3);
            BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos3) : null;
            PistonBlock.dropStacks(blockState, world, blockPos3, blockEntity);
            world.setBlockState(blockPos3, Blocks.AIR.getDefaultState(), Block.FORCE_STATE | Block.NOTIFY_LISTENERS);
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, blockPos3, GameEvent.Emitter.of(blockState));
            if (!blockState.isIn(BlockTags.FIRE)) {
                world.addBlockBreakParticles(blockPos3, blockState);
            }
            blockStates[i++] = blockState;
        }
        for (j = list.size() - 1; j >= 0; --j) {
            blockPos3 = list.get(j);
            BlockState blockState = world.getBlockState(blockPos3);
            blockPos3 = blockPos3.offset(direction);
            map.remove(blockPos3);
            BlockState blockState3 = progressionrespun$getWaxedOxidizedPiston(Blocks.MOVING_PISTON, this).getDefaultState().with(FACING, dir);
            world.setBlockState(blockPos3, blockState3, Block.MOVED | Block.NO_REDRAW);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(blockPos3, blockState3, list2.get(j), dir, retract, false));
            blockStates[i++] = blockState;
        }
        if (retract) {
            PistonType pistonType = this.sticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockState4 = progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON_HEAD, this).getDefaultState().with(PistonHeadBlock.FACING, dir).with(PistonHeadBlock.TYPE, pistonType);
            BlockState blockState = progressionrespun$getWaxedOxidizedPiston(Blocks.MOVING_PISTON, this).getDefaultState().with(PistonExtensionBlock.FACING, dir).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(blockPos);
            world.setBlockState(blockPos, blockState, Block.MOVED | Block.NO_REDRAW);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(blockPos, blockState, blockState4, dir, true, true));
        }
        BlockState blockState5 = Blocks.AIR.getDefaultState();
        for (BlockPos blockPos2 : map.keySet()) {
            world.setBlockState(blockPos2, blockState5, Block.FORCE_STATE | Block.MOVED | Block.NOTIFY_LISTENERS);
        }
        for (Map.Entry entry : map.entrySet()) {
            blockPos5 = (BlockPos) entry.getKey();
            BlockState blockState6 = (BlockState) entry.getValue();
            blockState6.prepare(world, blockPos5, 2);
            blockState5.updateNeighbors(world, blockPos5, Block.NOTIFY_LISTENERS);
            blockState5.prepare(world, blockPos5, 2);
        }
        i = 0;
        for (k = list3.size() - 1; k >= 0; --k) {
            BlockState blockState = blockStates[i++];
            blockPos5 = list3.get(k);
            blockState.prepare(world, blockPos5, 2);
            world.updateNeighborsAlways(blockPos5, blockState.getBlock());
        }
        for (k = list.size() - 1; k >= 0; --k) {
            world.updateNeighborsAlways(list.get(k), blockStates[i++].getBlock());
        }
        if (retract) {
            world.updateNeighborsAlways(blockPos, progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON_HEAD, this));
        }
        return true;
    }

    private boolean shouldExtend(RedstoneView world, BlockPos pos, Direction pistonFace) {
        for (Direction direction : Direction.values()) {
            if (direction == pistonFace || !world.isEmittingRedstonePower(pos.offset(direction), direction)) continue;
            return true;
        }
        if (world.isEmittingRedstonePower(pos, Direction.DOWN)) {
            return true;
        }
        BlockPos blockPos = pos.up();
        for (Direction direction2 : Direction.values()) {
            if (direction2 == Direction.DOWN || !world.isEmittingRedstonePower(blockPos.offset(direction2), direction2)) continue;
            return true;
        }
        return false;
    }

    public static boolean isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir) {
        if (pos.getY() < world.getBottomY() || pos.getY() > world.getTopY() - 1 || !world.getWorldBorder().contains(pos)) {
            return false;
        }
        if (state.isAir()) {
            return true;
        }
        if (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.CRYING_OBSIDIAN) || state.isOf(Blocks.RESPAWN_ANCHOR) || state.isOf(Blocks.REINFORCED_DEEPSLATE)) {
            return false;
        }
        if (direction == Direction.DOWN && pos.getY() == world.getBottomY()) {
            return false;
        }
        if (direction == Direction.UP && pos.getY() == world.getTopY() - 1) {
            return false;
        }
        if (state.isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.PISTON, state.getBlock())) || state.isOf(progressionrespun$getWaxedOxidizedPiston(Blocks.STICKY_PISTON, state.getBlock()))) {
            if (state.get(EXTENDED)) {
                return false;
            }
        } else {
            if (state.getHardness(world, pos) == -1.0f) {
                return false;
            }
            switch (state.getPistonBehavior()) {
                case BLOCK: {
                    return false;
                }
                case DESTROY: {
                    return canBreak;
                }
                case PUSH_ONLY: {
                    return direction == pistonDir;
                }
            }
        }
        return !state.hasBlockEntity();
    }
}
