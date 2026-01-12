package com.progression_respun.mixin.under_armor_handling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.progression_respun.ProgressionRespun;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow @Final
    PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;", ordinal = 1))
    private Item progressionrespun$sweepIfNotBroken(ItemStack itemStack) {
        if (itemStack.isDamageable() && itemStack.getDamage() >= itemStack.getMaxDamage()) {
            return null;
        }
        return itemStack.getItem();
    }

    @Inject(method = "updateTurtleHelmet", at = @At("HEAD"))
    private void progressionrespun$extendTurtleHelmet(CallbackInfo ci) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
        if (!this.isSubmergedIn(FluidTags.WATER) && itemStack.isOf(Items.TURTLE_HELMET)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 900, 0, false, false, true));
        }
    }

    @Unique
    private ItemStack progressionrespun$getEquippedArmor(ItemStack original, EquipmentSlot slot) {
        if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && original.isIn(UNDER_ARMOR)) {
            return ProgressionRespun.getArmor(original);
        }
        return ItemStack.EMPTY;
    }

    @WrapOperation(method = "vanishCursedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAnyEnchantmentsWith(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Z"))
    private boolean progressionrespun$vanishUnderArmor(ItemStack stack, ComponentType<?> componentType, Operation<Boolean> original) {
        ItemStack armorStack = ProgressionRespun.getArmor(stack);
        if (armorStack != ItemStack.EMPTY) return original.call(armorStack, componentType);
        return original.call(stack, componentType);
    }
}