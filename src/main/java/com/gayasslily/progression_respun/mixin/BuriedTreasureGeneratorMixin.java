package com.gayasslily.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.BuriedTreasureGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;

@Mixin(BuriedTreasureGenerator.Piece.class)
public class BuriedTreasureGeneratorMixin {

    @WrapOperation(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/BuriedTreasureGenerator$Piece;addChest(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/block/BlockState;)Z"))
    private boolean gay(BuriedTreasureGenerator.Piece instance, ServerWorldAccess serverWorldAccess, BlockBox blockBox, Random random, BlockPos pos, RegistryKey<LootTable> registryKey, BlockState state, Operation<Boolean> original, StructureWorldAccess world) {
        if (blockBox.contains(pos)){
            world.setBlockState(pos, Blocks.SUSPICIOUS_SAND.getDefaultState(), 2);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BrushableBlockEntity) {
                ItemStack bundle = new ItemStack(Items.BUNDLE);
                BundleContentsComponent bundleContentsComponent = bundle.get(DataComponentTypes.BUNDLE_CONTENTS);
                if (bundleContentsComponent != null) {
                    LootTable lootTable = Objects.requireNonNull(world.getServer()).getReloadableRegistries().getLootTable(registryKey);
                    LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorldAccess.toServerWorld()).build(LootContextType.create().build());
                    List<ItemStack> list = lootTable.generateLoot(lootContextParameterSet);
                    BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);
                    for (int i = 1; i < list.size(); i++) builder.add(list.get(i));
                    bundle.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
                }
                ((BrushableBlockEntity) blockEntity).item = bundle;
            }
            return true;
        }
        return false;
    }
}
