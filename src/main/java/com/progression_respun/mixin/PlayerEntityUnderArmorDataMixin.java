package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.access.UnderArmorItemStackAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.ProgressionRespun.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityUnderArmorDataMixin extends LivingEntity implements UnderArmorItemStackAccess {

    protected PlayerEntityUnderArmorDataMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initUnderArmorData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HEAD_UNDER_ARMOR, ItemStack.EMPTY);
        builder.add(CHEST_UNDER_ARMOR, ItemStack.EMPTY);
        builder.add(LEG_UNDER_ARMOR, ItemStack.EMPTY);
        builder.add(FEET_UNDER_ARMOR, ItemStack.EMPTY);
    }

    @WrapMethod(method = "writeCustomDataToNbt")
    private void writeUnderArmorDataToNbt(NbtCompound nbt, Operation<Void> original) {
        original.call(nbt);
        if (!progression_respun$getHeadUnderArmor().isEmpty())
            nbt.put("HeadUnderArmor", progression_respun$getHeadUnderArmor().encode(getRegistryManager()));
        if (!progression_respun$getChestUnderArmor().isEmpty())
            nbt.put("ChestUnderArmor", progression_respun$getChestUnderArmor().encode(getRegistryManager()));
        if (!progression_respun$getLegUnderArmor().isEmpty())
            nbt.put("LegUnderArmor", progression_respun$getLegUnderArmor().encode(getRegistryManager()));
        if (!progression_respun$getFeetUnderArmor().isEmpty())
            nbt.put("FeetUnderArmor", progression_respun$getFeetUnderArmor().encode(getRegistryManager()));
    }

    @WrapMethod(method = "readCustomDataFromNbt")
    private void readUnderArmorDataFromNbt(NbtCompound nbt, Operation<Void> original) {
        original.call(nbt);
        progression_respun$setHeadUnderArmor(ItemStack.fromNbt(getRegistryManager(), nbt.get("HeadUnderArmor")).orElse(ItemStack.EMPTY));
        progression_respun$setChestUnderArmor(ItemStack.fromNbt(getRegistryManager(), nbt.get("ChestUnderArmor")).orElse(ItemStack.EMPTY));
        progression_respun$setLegUnderArmor(ItemStack.fromNbt(getRegistryManager(), nbt.get("LegUnderArmor")).orElse(ItemStack.EMPTY));
        progression_respun$setFeetUnderArmor(ItemStack.fromNbt(getRegistryManager(), nbt.get("FeetUnderArmor")).orElse(ItemStack.EMPTY));
    }

    @Override
    public ItemStack progression_respun$getHeadUnderArmor() {
        return dataTracker.get(HEAD_UNDER_ARMOR);
    }

    @Override
    public ItemStack progression_respun$getChestUnderArmor() {
        return dataTracker.get(CHEST_UNDER_ARMOR);
    }

    @Override
    public ItemStack progression_respun$getLegUnderArmor() {
        return dataTracker.get(LEG_UNDER_ARMOR);
    }

    @Override
    public ItemStack progression_respun$getFeetUnderArmor() {
        return dataTracker.get(FEET_UNDER_ARMOR);
    }

    @Override
    public void progression_respun$setHeadUnderArmor(ItemStack stack) {
        dataTracker.set(HEAD_UNDER_ARMOR, stack);
    }

    @Override
    public void progression_respun$setChestUnderArmor(ItemStack stack) {
        dataTracker.set(CHEST_UNDER_ARMOR, stack);
    }

    @Override
    public void progression_respun$setLegUnderArmor(ItemStack stack) {
        dataTracker.set(LEG_UNDER_ARMOR, stack);
    }

    @Override
    public void progression_respun$setFeetUnderArmor(ItemStack stack) {
        dataTracker.set(FEET_UNDER_ARMOR, stack);
    }
}
