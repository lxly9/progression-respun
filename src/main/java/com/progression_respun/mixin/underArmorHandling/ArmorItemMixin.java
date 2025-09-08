package com.progression_respun.mixin.underArmorHandling;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void equipAndSwap(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(stack.getItem() instanceof ArmorItem armor)) return;
        String group = switch (armor.getSlotType()) {
            case HEAD -> "head";
            case CHEST -> "chest";
            case LEGS -> "legs";
            case FEET -> "feet";
            default -> null;
        };
        if (group == null) return;
        TrinketsApi.getTrinketComponent(user).ifPresent(component -> {
            Identifier tagId = Identifier.of("trinkets", group + "/under_armor_" + group);
            boolean hasUnderArmor = component.isEquipped(itemStack -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, tagId))
            );
            if (!hasUnderArmor) {
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        });
    }

    @Shadow
    @Final
    protected RegistryEntry<ArmorMaterial> material;

    @Inject(method = "getProtection", at = @At("HEAD"), cancellable = true)
    private void modifyProtection(CallbackInfoReturnable<Integer> ci) {
        ArmorMaterial mat = material.value();
        ArmorItem self = (ArmorItem) (Object) this;
        EquipmentSlot slot = self.getSlotType(); // public getter in 1.21.1

        if (mat.equals(ArmorMaterials.LEATHER)) {
            int value = switch (slot) {
                case HEAD, FEET, LEGS -> 1;
                case CHEST -> 2;
                default -> 0;
            };
            ci.setReturnValue(value);
        }

        if (mat.equals(ArmorMaterials.CHAIN)) {
            int value = switch (slot) {
                case HEAD, FEET, LEGS -> 2;
                case CHEST -> 3;
                default -> 0;
            };
            ci.setReturnValue(value);
        }
    }
}