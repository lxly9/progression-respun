package com.progression_respun.mixin.under_armor_handling;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
                    ordinal = 1
            )
    )
    private Item sweepIfNotBroken(ItemStack itemStack) {
        if (itemStack.isDamageable() && itemStack.getDamage() >= itemStack.getMaxDamage()) {
            return null;
        }
        return itemStack.getItem();
    }

    @Inject(method = "updateTurtleHelmet", at = @At("HEAD"))
    private void extendTurtleHelmet(CallbackInfo ci) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
        if (itemStack.isOf(Items.TURTLE_HELMET) && !this.isSubmergedIn(FluidTags.WATER)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 900, 0, false, false, true));
        }
    }

}