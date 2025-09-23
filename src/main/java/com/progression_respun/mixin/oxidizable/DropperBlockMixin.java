package com.progression_respun.mixin.oxidizable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(DropperBlock.class)
public class DropperBlockMixin extends DispenserBlock implements Oxidizable {

    @Unique
    private final Oxidizable.OxidationLevel oxidationLevel;

    public DropperBlockMixin(OxidationLevel oxidationLevel, AbstractBlock.Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    public void tickDegradation(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Oxidizable.super.tickDegradation(state, world, pos, random);
    }

    @Override
    public Oxidizable.OxidationLevel getDegradationLevel() {
        return this.oxidationLevel;
    }

    @Override
    public Optional<BlockState> tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        return Oxidizable.super.tryDegrade(state, world, pos, random);
    }
}
