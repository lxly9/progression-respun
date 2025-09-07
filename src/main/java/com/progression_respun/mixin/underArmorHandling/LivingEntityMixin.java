package com.progression_respun.mixin.underArmorHandling;

import dev.emi.trinkets.api.TrinketsApi;
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

import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "damageEquipment", at = @At("HEAD"))
    private void damageUnderArmor(DamageSource source, float amount, EquipmentSlot[] slots, CallbackInfo ci) {
        if (((Object) this) instanceof PlayerEntity player) {
            if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
                int damage = 0;
                if (player.getRandom().nextFloat() < 0.5f) {
                    damage = Math.round(amount);
                }
                if (damage <= 0) return;

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

                        ItemStack stack = player.getEquippedStack(armorSlot);
                        if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
                            EquipmentSlot slot = armorItem.getSlotType();

                            TagKey<Item> trinketTag = TagKey.of(RegistryKeys.ITEM, trinketTagId);

                            component.getEquipped(underArmorItem -> underArmorItem.isIn(trinketTag)).forEach(pair -> {
                                ItemStack trinketStack = pair.getRight();
                                if (!trinketStack.isEmpty()) {
                                    trinketStack.damage(1, player, slot);
                                }
                            });

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

                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        ci.setReturnValue(EquipmentSlot.MAINHAND);
                    }

                    if (!hasUnderArmor && !stack.isIn(BYPASSES_UNDER_ARMOR)) {
                        ci.setReturnValue(EquipmentSlot.MAINHAND);
                    }
                }
            }
        }
    }
}
