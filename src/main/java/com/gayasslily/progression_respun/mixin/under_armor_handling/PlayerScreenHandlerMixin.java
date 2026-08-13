package com.gayasslily.progression_respun.mixin.under_armor_handling;

import com.gayasslily.progression_respun.component.ModDataComponentTypes;
import com.gayasslily.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.gayasslily.progression_respun.ProgressionRespun.getArmor;
import static com.gayasslily.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;
import static com.gayasslily.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "quickMove", at = @At(value = "HEAD"), cancellable = true)
    private void quickMove(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot2 = this.slots.get(slot);
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = slot2.getStack();
        if (slot2.hasStack()) {
            itemStack = itemStack2.copy();
            if (itemStack.getItem() instanceof ArmorItem armorItem) {
                if (!itemStack.isIn(UNDER_ARMOR) && !itemStack.isIn(BYPASSES_UNDER_ARMOR)) {
                    EquipmentSlot slot1 = armorItem.getSlotType();
                    ItemStack underArmor = player.getEquippedStack(slot1);
                    ItemStack underStack = getArmor(underArmor);
                    if (underStack.isEmpty()) {
                        UnderArmorContentsComponent stackComponent = underArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                        if (stackComponent != null) {
                            UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(stackComponent);
                            builder.add(itemStack);
                            underArmor.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
                            slot2.setStack(ItemStack.EMPTY);
                            slot2.markDirty();
                            cir.setReturnValue(ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }
}
