package com.progression_respun.mixin.underArmorHandling;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

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

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            Map<EquipmentSlot, Identifier> slotTagMap = Map.of(
                    EquipmentSlot.HEAD, Identifier.of("trinkets", "head/under_armor_head"),
                    EquipmentSlot.CHEST, Identifier.of("trinkets", "chest/under_armor_chest"),
                    EquipmentSlot.LEGS, Identifier.of("trinkets", "legs/under_armor_legs"),
                    EquipmentSlot.FEET, Identifier.of("trinkets", "feet/under_armor_feet")
            );

            for (Map.Entry<EquipmentSlot, Identifier> entry : slotTagMap.entrySet()) {
                EquipmentSlot armorSlot = entry.getKey();
                Identifier trinketTagId = entry.getValue();

                boolean hasTrinket = component.isEquipped(stack ->
                        stack.isIn(TagKey.of(RegistryKeys.ITEM, trinketTagId))
                );

                if (!hasTrinket) {
                    ItemStack armorStack = player.getEquippedStack(armorSlot);
                    if (!armorStack.isEmpty()) {
                        boolean inserted = player.getInventory().insertStack(armorStack);
                        if (!inserted) {
                            player.dropItem(armorStack, false);
                        }
                        player.equipStack(armorSlot, ItemStack.EMPTY);
                    }
                }
            }
        });
    }
}