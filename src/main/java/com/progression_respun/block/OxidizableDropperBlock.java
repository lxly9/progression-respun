package com.progression_respun.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class OxidizableDropperBlock extends DropperBlock implements Oxidizable {
    public static final MapCodec<OxidizableDropperBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(OxidationLevel.CODEC.fieldOf("weathering_state").forGetter(OxidizableDropperBlock::getDegradationLevel), createSettingsCodec()).apply(instance, OxidizableDropperBlock::new));
    private final Oxidizable.OxidationLevel oxidationLevel;

    public OxidizableDropperBlock(Oxidizable.OxidationLevel oxidationLevel, AbstractBlock.Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    protected boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    public Oxidizable.OxidationLevel getDegradationLevel() {
        return this.oxidationLevel;
    }
}
