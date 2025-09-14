package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

import static com.progression_respun.ProgressionRespun.LOGGER;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @WrapWithCondition(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V"))
    private boolean finishUsing(ItemStack instance, int amount, LivingEntity entity, @Local(argsOnly = true) World world) {

        if (entity instanceof ServerPlayerEntity serverPlayer) {
            if (instance.getDamage() >= instance.getMaxDamage()){
                return true;
            } else {
                if (!serverPlayer.isInCreativeMode()) {
                    instance.damage(1, serverPlayer, EquipmentSlot.MAINHAND);

                    return false;
                }
            }
        }
        return false;
    }
}
