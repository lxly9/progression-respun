package com.progression_respun.mixin.underArmorHandling;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler self = (ScreenHandler)(Object)this;
        if (slotIndex < 0 || slotIndex >= self.slots.size()) return;

        Slot slot = self.getSlot(slotIndex);
        if (slot == null) return;

        ItemStack cursorStack = self.getCursorStack();
        if (cursorStack.getItem() instanceof ArmorItem armorItem) {
            EquipmentSlot equipSlot = armorItem.getSlotType();

            int armorSlot = switch (equipSlot) {
                case FEET -> 36;
                case LEGS -> 37;
                case CHEST -> 38;
                case HEAD -> 39;
                default -> -1;
            };

            if (slot.getIndex() == armorSlot && slot.inventory instanceof PlayerInventory) {
                String group = switch (equipSlot) {
                    case HEAD -> "head";
                    case CHEST -> "chest";
                    case LEGS -> "legs";
                    case FEET -> "feet";
                    default -> null;
                };

                if (group != null) {
                    Identifier tagId = Identifier.of("trinkets", group + "/under_armor_" + group);
                    boolean hasUnderArmor = TrinketsApi.getTrinketComponent(player).map(tc -> tc.isEquipped(itemStack -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, tagId)))).orElse(false);

                    if (!hasUnderArmor) {
                            ci.cancel();
                    }
                }
            }
        }
    }
}