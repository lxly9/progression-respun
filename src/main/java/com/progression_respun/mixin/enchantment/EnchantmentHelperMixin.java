package com.progression_respun.mixin.enchantment;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.ProgressionRespun;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

import static com.progression_respun.ProgressionRespun.*;
import static com.progression_respun.data.ModItemTagProvider.*;
import static net.minecraft.enchantment.EnchantmentHelper.getPossibleEntries;

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
                ItemStack armorStack = getArmor(stack);
                if (armorStack != ItemStack.EMPTY && !armorStack.isIn(UNDER_ARMOR)) {
                    forEachEnchantment(armorStack, equipmentSlot, entity, contextAwareConsumer);
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

    @Inject(method = "generateEnchantments", at = @At("HEAD"), cancellable = true)
    private static void progressionrespun$genOneEnchantment(Random random, ItemStack stack, int level, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (stack.getItem() instanceof EnchantedBookItem) {
            List<EnchantmentLevelEntry> list = Lists.<EnchantmentLevelEntry>newArrayList();
            Item item = stack.getItem();
            int i = item.getEnchantability();
            if (i <= 0) {
                cir.setReturnValue(list);
            } else {
                level += 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
                float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
                level = MathHelper.clamp(Math.round(level + level * f), 1, Integer.MAX_VALUE);
                List<EnchantmentLevelEntry> list2 = getPossibleEntries(level, stack, possibleEnchantments);
                if (!list2.isEmpty()) {
                    Weighting.getRandom(random, list2).ifPresent(list::add);
                }
                cir.setReturnValue(list);
            }
        }
    }

    @WrapMethod(method = "getFishingTimeReduction")
    private static float progressionrespun$lureWithBait(ServerWorld world, ItemStack stack, Entity user, Operation<Float> original) {
        ItemStack baitStack = getBait(stack);
        if (baitStack.isIn(TIME_REDUCTION_BAIT)) {
            return 10;
        }
        return 0;
    }

    @WrapMethod(method = "getFishingLuckBonus")
    private static int progressionrespun$luckWithBait(ServerWorld world, ItemStack stack, Entity user, Operation<Float> original) {
        ItemStack baitStack = getBait(stack);
        if (baitStack.isIn(LUCK_BAIT)) {
            return 2;
        }
        return 0;
    }
}
