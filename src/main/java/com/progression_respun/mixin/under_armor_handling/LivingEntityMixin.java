package com.progression_respun.mixin.under_armor_handling;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "damageEquipment", at = @At("HEAD"))
    private void damageUnderArmor(DamageSource source, float amount, EquipmentSlot[] slots, CallbackInfo ci) {
        if (((Object) this) instanceof PlayerEntity player) {
            if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
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

                        ItemStack armorStack = player.getEquippedStack(armorSlot);

                        TagKey<Item> trinketTag = TagKey.of(RegistryKeys.ITEM, trinketTagId);

                        int damageToApply;

                        if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem) {
                            if (player.getRandom().nextFloat() < 0.25f) {
                                damageToApply = Math.round(amount);
                            } else {
                                damageToApply = 0;
                            }
                        } else {
                            damageToApply = Math.round(amount);
                        }

                        if (damageToApply <= 0) continue;

                        component.getEquipped(trinketStack -> trinketStack.isIn(trinketTag)).forEach(pair -> {
                            ItemStack underArmorStack = pair.getRight();

                            if (!underArmorStack.isEmpty() && underArmorStack.getItem() instanceof ArmorItem armor && armorStack.getItem() instanceof ArmorItem armorItem) {
                                int armorPoints = armor.getProtection();
                                int armorPoints1 = armorItem.getProtection();
                                int scaledDamage = Math.max(1, Math.round(damageToApply * (armorPoints / 5f) * (armorPoints1 / 5f)));
                                underArmorStack.damage(scaledDamage, player, armorSlot);
                            }
                        });
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

                    boolean hasValidUnderArmor = TrinketsApi.getTrinketComponent(player)
                            .map(component -> component.isEquipped(stack2 ->
                                    stack2.isIn(TagKey.of(RegistryKeys.ITEM, tagId)) && stack2.getDamage() < stack2.getMaxDamage()
                            ))
                            .orElse(false);

                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        ci.setReturnValue(EquipmentSlot.MAINHAND);
                    }

                    if (!hasValidUnderArmor && !stack.isIn(BYPASSES_UNDER_ARMOR)) {
                        ci.setReturnValue(EquipmentSlot.MAINHAND);
                    }
                }
            }
        }
    }

    @Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
    private void addUnderArmor(CallbackInfoReturnable<Integer> ci) {
        if ((Object)this instanceof PlayerEntity player) {
            TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
                AtomicInteger bonusArmor = new AtomicInteger();
                component.getEquipped(stack -> stack.getItem() instanceof ArmorItem).forEach(pair -> {
                    ItemStack stack = pair.getRight();

                    if (stack.getDamage() >= stack.getMaxDamage()) return;
                    ArmorItem armor = (ArmorItem) pair.getRight().getItem();
                    bonusArmor.addAndGet(armor.getProtection());
                });
                ci.setReturnValue(ci.getReturnValue() + bonusArmor.get());
            });
        }
    }
}
