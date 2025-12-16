package com.progression_respun.mixin;

import com.progression_respun.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    @Shadow
    public abstract RegistryKey<LootTable> getLootTableKey();

    @Inject(method = "getDroppedStacks", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$dropWorm(BlockState state, LootContextParameterSet.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        RegistryKey<LootTable> registryKey = this.getLootTableKey();
        if (state.isIn(BlockTags.DIRT)) {
            LootContextParameterSet lootContextParameterSet = builder.add(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
            ServerWorld serverWorld = lootContextParameterSet.getWorld();
            LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(registryKey);
            List<ItemStack> loot = lootTable.generateLoot(lootContextParameterSet);
            ItemStack stack = new ItemStack(ModItems.WORM);
            Random random = serverWorld.getRandom();
            if (random.nextFloat() < 0.15f) {
                loot.addLast(stack);
            }
            cir.setReturnValue(loot);
        }
    }
}
