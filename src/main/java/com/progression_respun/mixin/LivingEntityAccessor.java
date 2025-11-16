package com.progression_respun.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("getSyncedArmorStack")
    ItemStack invokeGetSyncedArmorStack(EquipmentSlot slot);
    @Invoker("getSyncedHandStack")
    ItemStack invokeGetSyncedHandStack(EquipmentSlot slot);
    @Accessor("syncedBodyArmorStack")
    ItemStack getSyncedBodyArmorStack();
    @Accessor("syncedBodyArmorStack")
    void setSyncedBodyArmorStack(ItemStack stack);
}
