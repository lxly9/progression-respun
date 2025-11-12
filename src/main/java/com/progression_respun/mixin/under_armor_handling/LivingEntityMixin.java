package com.progression_respun.mixin.under_armor_handling;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "damageEquipment", at = @At("HEAD"))
    private void damageUnderArmor(DamageSource source, float amount, EquipmentSlot[] slots, CallbackInfo ci) {
        if (((Object)this) instanceof PlayerEntity player) {
            if (source.isIn(DamageTypeTags.BYPASSES_ARMOR)) return;

            for (EquipmentSlot armorSlot : EquipmentSlot.values()) {

                if (armorSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

                ItemStack outerArmor = player.getEquippedStack(armorSlot);
                float occupancy = UnderArmorContentsComponent.getAmountFilled(outerArmor);
                if (occupancy <= 0) return;

                if (outerArmor.isEmpty() || !(outerArmor.getItem() instanceof ArmorItem outerArmorItem)) continue;

                UnderArmorContentsComponent component = outerArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component == null) continue;

                ItemStack innerArmor = component.get(0);
                if (innerArmor.isEmpty() || !(innerArmor.getItem() instanceof ArmorItem innerArmorItem)) continue;

                int damageToApply;
                if (player.getRandom().nextFloat() < 0.25f) {
                    damageToApply = Math.round(amount);
                } else {
                    damageToApply = 0;
                }

                if (damageToApply <= 0) continue;

                int innerProtection = innerArmorItem.getProtection();
                int outerProtection = outerArmorItem.getProtection();
                int scaledDamage = Math.max(1, Math.round(damageToApply * (innerProtection / 5f) * (outerProtection / 5f)));

                innerArmor.damage(scaledDamage, player, armorSlot);
            }
        }
    }


    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void redirectArmorIfNoUnderArmor(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> ci) {
        if (stack.getItem() instanceof ArmorItem) {

            if ((Object) this instanceof PlayerEntity) {
                if (!stack.isIn(UNDER_ARMOR)) ci.setReturnValue(EquipmentSlot.MAINHAND);
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

                ItemStack outerArmor = player.getEquippedStack(armorSlot);
                float occupancy = UnderArmorContentsComponent.getAmountFilled(outerArmor);
                if (occupancy <= 0) return;

                if (outerArmor.isEmpty() || !(outerArmor.getItem() instanceof ArmorItem)) continue;

                UnderArmorContentsComponent component = outerArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component == null) continue;

                ItemStack innerArmor = component.get(0);
                if (innerArmor.isEmpty() || !(innerArmor.getItem() instanceof ArmorItem armor)) continue;
                if (innerArmor.getDamage() >= innerArmor.getMaxDamage()) continue;

                bonusArmor += armor.getProtection();
            }
            ci.setReturnValue(ci.getReturnValue() + bonusArmor);
        }
    }
}
