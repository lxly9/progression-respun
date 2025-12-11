package com.progression_respun.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @WrapMethod(method = "finishUsing")
    private ItemStack progressionrespun$finishUsing(ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original) {
        if (user instanceof PlayerEntity player) {

            if (player instanceof ServerPlayerEntity) {
                Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)player, stack);
            }

            if (!world.isClient) {
                PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                potionContentsComponent.forEachEffect(effect -> {
                    if (effect.getEffectType().value().isInstant()) {
                        effect.getEffectType().value().applyInstantEffect(player, player, user, effect.getAmplifier(), 1.0);
                    } else {
                        user.addStatusEffect(effect);
                    }
                });
            }

            player.incrementStat(Stats.USED.getOrCreateStat((PotionItem) (Object) this));
            user.emitGameEvent(GameEvent.DRINK);
            stack.damage(1, player, EquipmentSlot.MAINHAND);
            if (stack.getDamage() >= stack.getMaxDamage()) {
                stack.decrementUnlessCreative(1, player);
                player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            return stack;
        }
        original.call(stack, world, user);
        return stack;
    }
}
