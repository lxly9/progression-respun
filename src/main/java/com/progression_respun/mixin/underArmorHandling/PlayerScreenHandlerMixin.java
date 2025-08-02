package com.progression_respun.mixin.underArmorHandling;

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

        TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
            boolean canEquipInTrinket = trinketComponent.getInventory().entrySet().stream()
                    .flatMap(groupEntry -> groupEntry.getValue().entrySet().stream()
                            .flatMap(slotEntry -> {
                                var inventory = slotEntry.getValue();
                                return java.util.stream.IntStream.range(0, inventory.size())
                                        .mapToObj(i -> new dev.emi.trinkets.api.SlotReference(inventory, i));
                            })
                    )
                    .anyMatch(slotRef -> {
                        var trinket = TrinketsApi.getTrinket(stack.getItem());
                        return trinket != null && trinket.canEquip(stack, slotRef, player);
                    });
            if (canEquipInTrinket) {
                // Let trinket logic handle the transfer, do not block or redirect here
                return;
            }

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

            Identifier tagId = Identifier.of("trinkets", group + "/under_armor_" + group);

            boolean hasUnderArmor = trinketComponent.isEquipped(itemStack ->
                    itemStack.isIn(TagKey.of(RegistryKeys.ITEM, tagId))
            );

            if (!hasUnderArmor) {
                return;
            }
        });
    }
}