package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

//    @ModifyReturnValue(method = "canHaveEnchantments", at = @At("RETURN"))
//    private static boolean underArmorCantHaveEnchantments(boolean original, @Local(argsOnly = true) ItemStack stack) {
//        return !stack.isIn(ModItemTagProvider.UNDER_ARMOR) && stack.isEnchantable();
//    }

    @Shadow
    private static void forEachEnchantment(ItemStack stack, EquipmentSlot slot, LivingEntity entity, EnchantmentHelper.ContextAwareConsumer contextAwareConsumer) {
    }

    @WrapMethod(method = "forEachEnchantment(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V")
    private static void progressionrespun$forEachArmorEnchantment(LivingEntity entity, EnchantmentHelper.ContextAwareConsumer contextAwareConsumer, Operation<Void> original) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack stack = entity.getEquippedStack(equipmentSlot);
            if (stack.getItem() instanceof ArmorItem && stack.isIn(UNDER_ARMOR)) {
                UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component != null && !component.isEmpty()) {
                    ItemStack armor = component.get(0);
                    if (!armor.isEmpty() && armor.getItem() instanceof ArmorItem) {
                        forEachEnchantment(armor, equipmentSlot, entity, contextAwareConsumer);
                    }
                }
                forEachEnchantment(stack, equipmentSlot, entity, contextAwareConsumer);
            }
            original.call(entity, contextAwareConsumer);
        }
    }

    @ModifyReturnValue(method = "getRepairWithXp", at = @At("RETURN"))
    private static int progressionrespun$noMoreMending(int original) {
        return 0;
    }
}
