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
    ItemStack progressionrespun$getSyncedArmorStack(EquipmentSlot slot);
    @Invoker("getSyncedHandStack")
    ItemStack progressionrespun$getSyncedHandStack(EquipmentSlot slot);
    @Accessor("syncedBodyArmorStack")
    ItemStack progressionrespun$getSyncedBodyArmorStack();
    @Accessor("syncedBodyArmorStack")
    void progressionrespun$setSyncedBodyArmorStack(ItemStack stack);
}
