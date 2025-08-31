package com.progression_respun.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.TrinketInventory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract void sendEquipmentBreakStatus(Item item, EquipmentSlot slot);

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "damageArmor", at = @At("HEAD"))
    private void damageUnderArmor(DamageSource source, float amount, CallbackInfo ci) {
        if (((Object) this) instanceof PlayerEntity playerEntity) {
            if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
                int damage = Math.round(amount / 2.0f);
                if (damage <= 0) return;

                TrinketsApi.getTrinketComponent(playerEntity).ifPresent(component -> {
                    Map<String, Map<String, TrinketInventory>> inventory = component.getInventory();

                    for (Map<String, TrinketInventory> group : inventory.values()) {
                        for (TrinketInventory trinketInv : group.values()) {
                            for (int i = 0; i < trinketInv.size(); i++) {
                                ItemStack stack = trinketInv.getStack(i);
                                Item item = stack.getItem();
                                if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
                                    EquipmentSlot slot = armorItem.getSlotType();
                                    sendEquipmentBreakStatus(item, slot);
                                }
                            }
                        }
                    }
                });

            }
        }
    }
    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void redirectArmorIfNoUnderArmor(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> ci) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            EquipmentSlot defaultSlot = armorItem.getSlotType();

            if ((Object) this instanceof PlayerEntity player) {

                String group = switch (defaultSlot) {
                    case HEAD -> "head";
                    case CHEST -> "chest";
                    case LEGS -> "legs";
                    case FEET -> "feet";
                    default -> null;
                };

                if (group != null) {
                    Identifier tagId = Identifier.of("trinkets", group + "/under_armor_" + group);

                    boolean hasUnderArmor = TrinketsApi.getTrinketComponent(player).map(tc -> tc.isEquipped(stack2 -> stack2.isIn(TagKey.of(RegistryKeys.ITEM, tagId)))).orElse(false);

                    if (!hasUnderArmor) {
                        ci.setReturnValue(EquipmentSlot.MAINHAND);
                    }
                }
            }
        }
    }
}
