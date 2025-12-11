package com.progression_respun.mixin.under_armor_handling;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.mixin.LivingEntityAccessor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Random;

import static com.progression_respun.ProgressionRespun.getArmor;
import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow @Final private AttributeContainer attributes;

    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @WrapOperation(method = "damageEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"))
    private void progressionrespun$damageUnderArmor(ItemStack item, int amount, LivingEntity entity, EquipmentSlot slot, Operation<Void> underArmor, @Local(argsOnly = true) DamageSource source) {
        if (((Object) this) instanceof PlayerEntity player) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                if (!item.isEmpty() && item.getItem() instanceof ArmorItem) {
                    ItemStack armorStack = getArmor(item);
                    if (armorStack != ItemStack.EMPTY) {
                        Random random = new Random();
                        if (armorStack.takesDamageFrom(source)) {
                            armorStack.damage(amount, player, slot);
                            if (random.nextDouble() < 0.25) {
                                underArmor.call(item, amount, entity, slot);
                            }
                        }
                    }
                    underArmor.call(item, amount, entity, slot);
                }
            }
        }
        underArmor.call(item, amount, entity, slot);
    }

    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void progressionrespun$redirectArmorIfNoUnderArmor(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> ci) {
        if (stack.getItem() instanceof ArmorItem) {

            if ((Object) this instanceof PlayerEntity) {
                if (!stack.isIn(UNDER_ARMOR) && !stack.isIn(BYPASSES_UNDER_ARMOR)) ci.setReturnValue(EquipmentSlot.MAINHAND);
                if (stack.getDamage() >= stack.getMaxDamage()) ci.setReturnValue(EquipmentSlot.MAINHAND);
            }
        }
    }

    @WrapMethod(method = "getEquipmentChanges")
    private @Nullable Map<EquipmentSlot, ItemStack> progressionrespun$getArmorChanges(Operation<Map<EquipmentSlot, ItemStack>> original) {
        Map<EquipmentSlot, ItemStack> map = null;
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack var10000;
            switch (equipmentSlot.getType()) {
                case HAND -> var10000 = ((LivingEntityAccessor) livingEntity).progressionrespun$getSyncedHandStack(equipmentSlot);
                case HUMANOID_ARMOR -> var10000 = ((LivingEntityAccessor) livingEntity).progressionrespun$getSyncedArmorStack(equipmentSlot);
                case ANIMAL_ARMOR -> var10000 = ((LivingEntityAccessor) livingEntity).progressionrespun$getSyncedBodyArmorStack();
                default -> throw new MatchException(null, null);
            }

            ItemStack itemStack = var10000;
            ItemStack itemStack2 = livingEntity.getEquippedStack(equipmentSlot);
            if (livingEntity.areItemsDifferent(itemStack, itemStack2)) {
                if (map == null) {
                    map = Maps.newEnumMap(EquipmentSlot.class);
                }

                map.put(equipmentSlot, itemStack2);
                AttributeContainer attributeContainer = livingEntity.getAttributes();
                if (!itemStack.isEmpty()) {
                    itemStack.applyAttributeModifiers(equipmentSlot, (attribute, modifier) -> {
                        EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
                        if (entityAttributeInstance != null) {
                            entityAttributeInstance.removeModifier(modifier);
                        }

                        EnchantmentHelper.removeLocationBasedEffects(itemStack, livingEntity, equipmentSlot);
                    });
                    if (itemStack.getItem() instanceof ArmorItem && itemStack.isIn(UNDER_ARMOR)) {
                        ItemStack armorStack = getArmor(itemStack);
                        if (armorStack == ItemStack.EMPTY) continue;

                        armorStack.applyAttributeModifiers(equipmentSlot, (attribute, modifier) -> {
                            EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
                            EntityAttributeModifier modifier1 = new EntityAttributeModifier(modifier.id().withSuffixedPath("_under_armor"), modifier.value(), modifier.operation());
                            if (entityAttributeInstance != null) {
                                entityAttributeInstance.removeModifier(modifier1);
                            }
                            EnchantmentHelper.removeLocationBasedEffects(armorStack, livingEntity, equipmentSlot);
                        });
                    }
                }
            }
        }

        if (map != null) {
            for(Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
                EquipmentSlot equipmentSlot2 = entry.getKey();
                ItemStack itemStack3 = entry.getValue();
                if (!itemStack3.isEmpty()) {
                    itemStack3.applyAttributeModifiers(equipmentSlot2, (registryEntry, entityAttributeModifier) -> {
                        EntityAttributeInstance entityAttributeInstance = livingEntity.getAttributes().getCustomInstance(registryEntry);
                        if (entityAttributeInstance != null) {
                            entityAttributeInstance.removeModifier(entityAttributeModifier.id());
                            entityAttributeInstance.addTemporaryModifier(entityAttributeModifier);
                        }

                        World world = livingEntity.getWorld();
                        if (world instanceof ServerWorld serverWorld) {
                            EnchantmentHelper.applyLocationBasedEffects(serverWorld, itemStack3, livingEntity, equipmentSlot2);
                        }

                    });
                    if (itemStack3.getItem() instanceof ArmorItem && itemStack3.isIn(UNDER_ARMOR)) {
                        ItemStack armorStack = getArmor(itemStack3);
                        if (armorStack == ItemStack.EMPTY) continue;

                        armorStack.applyAttributeModifiers(equipmentSlot2, (registryEntry, entityAttributeModifier) -> {
                            World world;
                            EntityAttributeInstance entityAttributeInstance = this.attributes.getCustomInstance(registryEntry);
                            EntityAttributeModifier entityAttributeModifier1 = new EntityAttributeModifier(entityAttributeModifier.id().withSuffixedPath("_under_armor"), entityAttributeModifier.value(), entityAttributeModifier.operation());
                            if (entityAttributeInstance != null) {
                                entityAttributeInstance.removeModifier(entityAttributeModifier1.id());
                                entityAttributeInstance.addTemporaryModifier(entityAttributeModifier1);
                            }
                            if ((world = livingEntity.getWorld()) instanceof ServerWorld) {
                                ServerWorld serverWorld = (ServerWorld) world;
                                EnchantmentHelper.applyLocationBasedEffects(serverWorld, armorStack, livingEntity, equipmentSlot2);
                            }
                        });
                    }
                }
            }
        }
        return map;
    }

    @WrapMethod(method = "dropLoot")
    private void progressionrespun$ironGolemsDontDrop(DamageSource damageSource, boolean causedByPlayer, Operation<Void> original) {
        if ((LivingEntity) (Object) this instanceof IronGolemEntity ironGolemEntity && !ironGolemEntity.isPlayerCreated()) {
            return;
        }
        original.call(damageSource, causedByPlayer);
    }
}
