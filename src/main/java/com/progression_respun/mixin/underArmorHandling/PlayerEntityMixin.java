package com.progression_respun.mixin.underArmorHandling;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow @Final private static Logger LOGGER;

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

    @Inject(method = "canEquip", at = @At("HEAD"), cancellable = true)
    private void canEquipArmor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof PlayerEntity player) {
            if (stack.getItem() instanceof ArmorItem armorItem) {
                EquipmentSlot equipSlot = armorItem.getSlotType();

                String group = switch (equipSlot) {
                    case HEAD -> "head";
                    case CHEST -> "chest";
                    case LEGS -> "legs";
                    case FEET -> "feet";
                    default -> null;
                };

                if (group != null) {
                    Identifier tagId = Identifier.of("trinkets", group + "/under_armor_" + group);

                    boolean hasUnderArmor = TrinketsApi.getTrinketComponent(player)
                            .map(tc -> tc.isEquipped(itemStack -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, tagId))))
                            .orElse(false);

                    if (!hasUnderArmor && !stack.isIn(BYPASSES_UNDER_ARMOR) && !(stack.getDamage() >= stack.getMaxDamage())) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
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

                boolean hasValidTrinket = component.isEquipped(stack ->
                        stack.isIn(TagKey.of(RegistryKeys.ITEM, trinketTagId)) && stack.getDamage() < stack.getMaxDamage()
                );

                if (!hasValidTrinket) {
                    ItemStack armorStack = player.getEquippedStack(armorSlot);
                    if (!armorStack.isEmpty() && (!armorStack.isIn(BYPASSES_UNDER_ARMOR) || armorStack.getDamage() >= armorStack.getMaxDamage())) {
                        boolean inserted = player.getInventory().insertStack(armorStack);
                        if (!inserted) {
                            player.dropItem(armorStack, true);
                        }
                        player.equipStack(armorSlot, ItemStack.EMPTY);
                    }
                }
            }
        });
    }

}