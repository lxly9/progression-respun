package com.progression_respun.mixin.under_armor_handling;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;

import static com.progression_respun.ProgressionRespun.getArmor;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {

    @WrapMethod(method = "canWalkOnPowderSnow")
    private static boolean progressionrespun$canWalkOnPowderSnow(Entity entity, Operation<Boolean> original) {
        if (entity instanceof PlayerEntity player) {
            ItemStack stack = player.getEquippedStack(EquipmentSlot.FEET);
            ItemStack armorStack = getArmor(stack);

            if (stack.isOf(Items.LEATHER_BOOTS)) {
                return armorStack.isEmpty() || armorStack.isOf(Items.LEATHER_BOOTS);
            } else {
                return armorStack.isOf(Items.LEATHER_BOOTS);
            }
        }
        return original.call(entity);
    }
}
