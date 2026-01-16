package com.progression_respun.mixin.under_armor_handling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.progression_respun.ProgressionRespun;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.progression_respun.ProgressionRespun.hasBinding;

@Mixin(targets = "net.minecraft.screen.slot.ArmorSlot")
public class ArmorSlotMixin {
    @WrapOperation(method = "canTakeItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAnyEnchantmentsWith(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Z"))
    private boolean progressionrespun$blockUnderArmorUnequip(ItemStack stack, ComponentType<?> componentType, Operation<Boolean> original) {
        ItemStack armorStack = ProgressionRespun.getArmor(stack);
        if (hasBinding(armorStack)) return original.call(armorStack, componentType);
        if (hasBinding(stack)) return original.call(stack, componentType);
        return original.call(stack, componentType);
    }
}
