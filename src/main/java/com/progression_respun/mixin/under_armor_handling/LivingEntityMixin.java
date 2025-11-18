package com.progression_respun.mixin.under_armor_handling;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import com.progression_respun.mixin.LivingEntityAccessor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow @Final private AttributeContainer attributes;

    @WrapOperation(method = "damageEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"))
    private void damageUnderArmor(ItemStack item, int amount, LivingEntity entity, EquipmentSlot slot, Operation<Void> underArmor, @Local(argsOnly = true) DamageSource source) {
        if (((Object) this) instanceof PlayerEntity player) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                if (!item.isEmpty() && item.getItem() instanceof ArmorItem) {
                    var component = item.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                    if (component != null && !component.isEmpty()) {
                        ItemStack armor = component.get(0);
                        Random random = new Random();
                        if (armor.takesDamageFrom(source)) {
                            armor.damage(amount, player, slot);
                            if (random.nextDouble() < 0.25) {
                                underArmor.call(item, amount, entity, slot);
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void redirectArmorIfNoUnderArmor(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> ci) {
        if (stack.getItem() instanceof ArmorItem) {

            if ((Object) this instanceof PlayerEntity) {
                if (!stack.isIn(UNDER_ARMOR) && !stack.isIn(BYPASSES_UNDER_ARMOR)) ci.setReturnValue(EquipmentSlot.MAINHAND);
                if (stack.getDamage() >= stack.getMaxDamage()) ci.setReturnValue(EquipmentSlot.MAINHAND);
            }
        }
    }

    @WrapMethod(method = "getEquipmentChanges")
    private @Nullable Map<EquipmentSlot, ItemStack> getArmorChanges(Operation<Map<EquipmentSlot, ItemStack>> original) {
        EnumMap<EquipmentSlot, ItemStack> map = null;
        if ((LivingEntity) (Object) this instanceof PlayerEntity player){
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = switch (equipmentSlot.getType()) {
                    default -> throw new MatchException(null, null);
                    case EquipmentSlot.Type.HAND -> ((LivingEntityAccessor) player).invokeGetSyncedHandStack(equipmentSlot);
                    case EquipmentSlot.Type.HUMANOID_ARMOR -> ((LivingEntityAccessor) player).invokeGetSyncedArmorStack(equipmentSlot);
                    case EquipmentSlot.Type.ANIMAL_ARMOR -> ((LivingEntityAccessor) player).getSyncedBodyArmorStack();
                };
                ItemStack itemStack2 = player.getEquippedStack(equipmentSlot);

                if (!player.areItemsDifferent(itemStack, itemStack2)) continue;
                if (map == null) {
                    map = Maps.newEnumMap(EquipmentSlot.class);
                }
                map.put(equipmentSlot, itemStack2);
                AttributeContainer attributeContainer = player.getAttributes();
                if (itemStack.isEmpty()) continue;
                itemStack.applyAttributeModifiers(equipmentSlot, (attribute, modifier) -> {
                    EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
                    if (entityAttributeInstance != null) {
                        entityAttributeInstance.removeModifier(modifier);
                    }
                    EnchantmentHelper.removeLocationBasedEffects(itemStack, player, equipmentSlot);
                });

                if (!(itemStack.getItem() instanceof ArmorItem) && !itemStack.isIn(UNDER_ARMOR)) continue;

                UnderArmorContentsComponent component = itemStack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                if (component == null || component.isEmpty()) continue;

                ItemStack armor = component.get(0);
                if (armor.isEmpty() || !(armor.getItem() instanceof ArmorItem)) continue;

                armor.applyAttributeModifiers(equipmentSlot, (attribute, modifier) -> {
                    EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
                    EntityAttributeModifier modifier1 = new EntityAttributeModifier(modifier.id().withSuffixedPath("_under_armor"), modifier.value(), modifier.operation());
                    if (entityAttributeInstance != null) {
                        entityAttributeInstance.removeModifier(modifier);
                        entityAttributeInstance.removeModifier(modifier1);
                    }
                    EnchantmentHelper.removeLocationBasedEffects(armor, player, equipmentSlot);
                });
            }
            if (map != null) {
                for (Map.Entry entry : map.entrySet()) {
                    EquipmentSlot equipmentSlot2 = (EquipmentSlot) entry.getKey();
                    ItemStack itemStack3 = (ItemStack) entry.getValue();
                    if (itemStack3.isEmpty()) continue;
                    itemStack3.applyAttributeModifiers(equipmentSlot2, (registryEntry, entityAttributeModifier) -> {
                        World world;
                        EntityAttributeInstance entityAttributeInstance = this.attributes.getCustomInstance(registryEntry);
                        if (entityAttributeInstance != null) {
                            entityAttributeInstance.removeModifier(entityAttributeModifier.id());
                            entityAttributeInstance.addTemporaryModifier(entityAttributeModifier);
                        }
                        if ((world = player.getWorld()) instanceof ServerWorld) {
                            ServerWorld serverWorld = (ServerWorld) world;
                            EnchantmentHelper.applyLocationBasedEffects(serverWorld, itemStack3, player, equipmentSlot2);
                        }
                    });

                    if (!(itemStack3.getItem() instanceof ArmorItem) && !itemStack3.isIn(UNDER_ARMOR)) continue;

                    UnderArmorContentsComponent component = itemStack3.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                    if (component == null || component.isEmpty()) continue;

                    ItemStack armor = component.get(0);
                    if (armor.isEmpty() || !(armor.getItem() instanceof ArmorItem)) continue;

                    armor.applyAttributeModifiers(equipmentSlot2, (registryEntry, entityAttributeModifier) -> {
                        World world;
                        EntityAttributeInstance entityAttributeInstance = this.attributes.getCustomInstance((RegistryEntry<EntityAttribute>) registryEntry);
                        EntityAttributeModifier entityAttributeModifier1 = new EntityAttributeModifier(entityAttributeModifier.id().withSuffixedPath("_under_armor"), entityAttributeModifier.value(), entityAttributeModifier.operation());
                        if (entityAttributeInstance != null) {
                            entityAttributeInstance.removeModifier(entityAttributeModifier1.id());
                            entityAttributeInstance.addTemporaryModifier(entityAttributeModifier1);
                        }
                        if ((world = player.getWorld()) instanceof ServerWorld) {
                            ServerWorld serverWorld = (ServerWorld) world;
                            EnchantmentHelper.applyLocationBasedEffects(serverWorld, armor, player, equipmentSlot2);
                        }
                    });
                }
            }
        }
        return map;
    }
}
