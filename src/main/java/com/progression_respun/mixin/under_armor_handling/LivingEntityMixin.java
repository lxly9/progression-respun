package com.progression_respun.mixin.under_armor_handling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow @Final private static Logger LOGGER;

    @WrapOperation(method = "damageEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"))
    private void damageUnderArmor(ItemStack item, int amount, LivingEntity entity, EquipmentSlot slot, Operation<Void> underArmor, @Local(argsOnly = true) DamageSource source) {
        if (((Object) this) instanceof PlayerEntity player) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                if (!item.isEmpty() && item.getItem() instanceof ArmorItem) {
                    boolean hasArmor = UnderArmorContentsComponent.getAmountFilled(item) > 0;
                    if (hasArmor) {
                        var component = item.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                        if (component != null) {
                            ItemStack armor = component.get(0);
                            Random random = new Random();
                            if (armor.takesDamageFrom(source)) {
                                armor.damage(amount, player, slot);
                            }
                            if (random.nextDouble() < 0.25) {
                                underArmor.call(item, amount, entity, slot);
                            }
                            return;
                        }
                    }
                }
            }
        }

        underArmor.call(item, amount, entity, slot);
    }

    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void redirectArmorIfNoUnderArmor(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> ci) {
        if (stack.getItem() instanceof ArmorItem) {

            if ((Object) this instanceof PlayerEntity) {
                if (!stack.isIn(UNDER_ARMOR) && !stack.isIn(BYPASSES_UNDER_ARMOR)) ci.setReturnValue(EquipmentSlot.MAINHAND);
                if (stack.getDamage() >= stack.getMaxDamage()) ci.setReturnValue(EquipmentSlot.MAINHAND);
            }
        }
    }

    @Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
    private void addArmorToUnderArmor(CallbackInfoReturnable<Integer> ci) {
        if ((Object)this instanceof PlayerEntity player) {

            int bonusArmor = 0;

            for (EquipmentSlot armorSlot : EquipmentSlot.values()) {
                if (armorSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

                ItemStack underArmor = player.getEquippedStack(armorSlot);
                float occupancy = UnderArmorContentsComponent.getAmountFilled(underArmor);
                if (occupancy <= 0) continue;

                if (underArmor.isEmpty() || !(underArmor.getItem() instanceof ArmorItem)) continue;

                UnderArmorContentsComponent component = underArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component == null) continue;

                ItemStack armorItem = component.get(0);
                if (armorItem.isEmpty() || !(armorItem.getItem() instanceof ArmorItem armor)) continue;
                if (armorItem.getDamage() >= armorItem.getMaxDamage()) continue;

                bonusArmor += armor.getProtection();
            }
            ci.setReturnValue(ci.getReturnValue() + bonusArmor);
        }
    }
}
