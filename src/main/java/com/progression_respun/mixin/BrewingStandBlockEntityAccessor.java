package com.progression_respun.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BrewingStandBlockEntity.class)
public interface BrewingStandBlockEntityAccessor {
    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();

    @Accessor("brewTime")
    int getBrewTime();
    @Accessor("brewTime")
    void setBrewTime(int time);

    @Accessor("fuel")
    int getFuel();
    @Accessor("fuel")
    void setFuel(int fuel);

    @Accessor("itemBrewing")
    Item getItemBrewing();
    @Accessor("itemBrewing")
    void setItemBrewing(Item item);

    @Accessor("slotsEmptyLastTick")
    boolean[] callSlotsEmptyLAstTick();
}
