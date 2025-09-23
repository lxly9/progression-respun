package com.progression_respun.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableDispenserBlock extends DispenserBlock implements Oxidizable, FabricBlockEntityType {
    public static final MapCodec<OxidizableDispenserBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(OxidationLevel.CODEC.fieldOf("weathering_state").forGetter(OxidizableDispenserBlock::getDegradationLevel), createSettingsCodec()).apply(instance, OxidizableDispenserBlock::new));
    private final OxidationLevel oxidationLevel;

    public OxidizableDispenserBlock(OxidationLevel oxidationLevel, Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    protected boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    public OxidationLevel getDegradationLevel() {
        return this.oxidationLevel;
    }
}
