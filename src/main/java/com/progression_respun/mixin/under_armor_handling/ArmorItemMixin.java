package com.progression_respun.mixin.under_armor_handling;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

import static com.progression_respun.ProgressionRespun.LOGGER;

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
}