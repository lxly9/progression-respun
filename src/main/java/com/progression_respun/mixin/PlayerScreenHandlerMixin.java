package com.progression_respun.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public class PlayerScreenHandlerMixin {

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void onTransferSlot(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        PlayerScreenHandler handler = (PlayerScreenHandler)(Object)this;
        Slot slot = handler.slots.get(index);
        if (slot == null || !slot.hasStack()) return;
        ItemStack stack = slot.getStack();
        if (!(stack.getItem() instanceof ArmorItem)) return;

        EquipmentSlot equipSlot = ((ArmorItem) stack.getItem()).getSlotType();

        String group = switch (equipSlot) {
            case HEAD -> "head";
            case CHEST -> "chest";
            case LEGS -> "legs";
            case FEET -> "feet";
            default -> null;
        };

        if (group == null) return;

        TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
            Identifier tagId = Identifier.of("trinkets", group + "/under_armor_" + group);

            boolean hasUnderArmor = trinketComponent.isEquipped(itemStack ->
                    itemStack.isIn(TagKey.of(RegistryKeys.ITEM, tagId))
            );

            if (!hasUnderArmor) {
                cir.setReturnValue(ItemStack.EMPTY);
                cir.cancel();
            }
        });
    }
}

