package com.gayasslily.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.BuriedTreasureGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BuriedTreasureGenerator.Piece.class)
public class BuriedTreasureGeneratorMixin {

    @WrapOperation(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/BuriedTreasureGenerator$Piece;addChest(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/block/BlockState;)Z"))
    private boolean gay(BuriedTreasureGenerator.Piece instance, ServerWorldAccess serverWorldAccess, BlockBox blockBox, Random random, BlockPos pos, RegistryKey registryKey, BlockState state, Operation<Boolean> original, StructureWorldAccess world, @Local BlockPos mutable) {
        if (blockBox.contains(mutable)){
            world.setBlockState(mutable, Blocks.SUSPICIOUS_SAND.getDefaultState(), 2);
            BlockEntity blockEntity = world.getBlockEntity(mutable);
            if (blockEntity instanceof BrushableBlockEntity) {
                ((BrushableBlockEntity) blockEntity).setLootTable(LootTables.BURIED_TREASURE_CHEST, random.nextLong());
            }
            return true;
        }
        return false;
    }
}
