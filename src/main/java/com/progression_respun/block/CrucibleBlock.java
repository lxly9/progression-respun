package com.progression_respun.block;

import com.mojang.serialization.MapCodec;
import com.progression_respun.block.entity.CrucibleBlockEntity;
import com.progression_respun.block.entity.ModBlockEntities;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlock extends BlockWithEntity implements BlockEntityProvider, Waterloggable {

    public static final MapCodec<CrucibleBlock> CODEC = CrucibleBlock.createCodec(CrucibleBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 11.0D, 14.0D);
    public static final BooleanProperty ONCAMPFIRE = BooleanProperty.of("on_campfire");
    public static final BooleanProperty HEATED = BooleanProperty.of("heated");
    public static final DirectionProperty FACING;

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public CrucibleBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(ONCAMPFIRE, false));
        setDefaultState(getDefaultState().with(HEATED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (state.getBlock() != newState.getBlock()) {
            if (blockEntity instanceof CrucibleBlockEntity) {
                ItemScatterer.spawn(world, pos,((CrucibleBlockEntity) blockEntity));
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }



    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof CrucibleBlockEntity crucibleBlockEntity) {
            ItemStack inputStack = crucibleBlockEntity.getStack(0);
            ItemStack outputStack = crucibleBlockEntity.getStack(1);
            if (!outputStack.isEmpty()) {
                player.getInventory().insertStack(outputStack.copy());
                crucibleBlockEntity.setStack(1, ItemStack.EMPTY);
                crucibleBlockEntity.markDirty();
                world.updateListeners(pos, state, state, 0);
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                return ItemActionResult.success(true);
            } else if (!stack.isEmpty()) {
                if (inputStack.isEmpty() && stack.isIn(ConventionalItemTags.RAW_MATERIALS)) {
                    crucibleBlockEntity.setStack(0, stack.copy().split(1));
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    world.playSound(player, pos, SoundEvents.BLOCK_COPPER_BULB_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    crucibleBlockEntity.markDirty();
                    world.updateListeners(pos, state, state, 0);
                    return ItemActionResult.success(stack.isIn(ConventionalItemTags.RAW_MATERIALS));
                } else if (inputStack.itemMatches(stack.getRegistryEntry()) && inputStack.getCount() < 16) {
                    inputStack.increment(1);
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    world.playSound(player, pos, SoundEvents.BLOCK_COPPER_BULB_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    crucibleBlockEntity.markDirty();
                    world.updateListeners(pos, state, state, 0);
                    return ItemActionResult.success(true);
                }
            }
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return null;
        }
        return validateTicker(type, ModBlockEntities.CRUCIBLE_BLOCK_ENTITY, ((world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1)));
    }

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
        builder.add(ONCAMPFIRE);
        builder.add(HEATED);
        builder.add(FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (ctx.getWorld().getBlockState(ctx.getBlockPos().offset(Direction.Axis.Y,-1)).isIn(BlockTags.CAMPFIRES)) {
            return this.getDefaultState()
                    .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER))
                    .with(FACING, ctx.getHorizontalPlayerFacing())
                    .with(HEATED, ctx.getWorld().getBlockState(ctx.getBlockPos().offset(Direction.Axis.Y,-1)).get(Properties.LIT))
                    .with(ONCAMPFIRE, true);
        }
        return this.getDefaultState()
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER))
                .with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public static int getLuminance(BlockState currentBlockState) {
        boolean activated = currentBlockState.get(CrucibleBlock.HEATED);

        return activated ? 7 : 0;
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    static {
        FACING = Properties.HORIZONTAL_FACING;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            BlockState blockBelow = world.getBlockState(pos.offset(Direction.Axis.Y, -1));
            boolean isOnCampfire = blockBelow.isIn(BlockTags.CAMPFIRES);
            boolean isHeated = isOnCampfire && blockBelow.get(Properties.LIT);

            BlockState newState = state
                    .with(ONCAMPFIRE, isOnCampfire)
                    .with(HEATED, isHeated);

            if (!state.equals(newState)) {
                world.setBlockState(pos, newState, Block.NOTIFY_ALL);
            }
        }
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }
}