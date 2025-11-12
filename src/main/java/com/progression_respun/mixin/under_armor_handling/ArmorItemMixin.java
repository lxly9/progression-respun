package com.progression_respun.mixin.under_armor_handling;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import com.progression_respun.util.SoundUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void equipAndSwap(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(stack.getItem() instanceof ArmorItem) || !stack.isIn(UNDER_ARMOR)) cir.setReturnValue(TypedActionResult.fail(stack));
    }

    public ArmorItemMixin(Settings settings) {
        super(settings.component(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, new UnderArmorContentsComponent(new ArrayList<>())));
    }

    @Unique
    private static float getAmountFilled(ItemStack stack) {
        UnderArmorContentsComponent component = stack.getOrDefault(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, UnderArmorContentsComponent.DEFAULT);
        return component.getOccupancy().floatValue();
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        int i;
        if (clickType != ClickType.RIGHT) return false;
        if (!UnderArmorContentsComponent.hasArmorSlot(stack)) return false;

        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component == null) return false;

        ItemStack itemStack = slot.getStack();
        if (!UnderArmorContentsComponent.isAllowedInUnderArmor(stack)) return false;
        UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(component);

        ArmorItem armorItem = (ArmorItem) stack.getItem();

        if (itemStack.isEmpty()) {
            SoundUtil.playRemoveOneSound(player);
            ItemStack itemStack2 = builder.removeFirst();
            if (itemStack2 != null) {
                ItemStack itemStack3 = slot.insertStack(itemStack2);
                builder.add(itemStack3);
            }
        } else if (itemStack.getItem() instanceof ArmorItem otherArmor && !(armorItem.getSlotType() == otherArmor.getSlotType())) {
            return false;
        } else if (itemStack.getItem() instanceof ElytraItem) {
            return false;
        } else if (itemStack.getItem().canBeNested() && (i = builder.add(slot, player)) > 0) {
            SoundUtil.playInsertSound(player);
        }
        stack.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
        return true;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {

        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) return false;
        if (!UnderArmorContentsComponent.hasArmorSlot(stack)) return false;

        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component == null) return false;
        UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(component);
        if (otherStack.isEmpty()) {
            ItemStack itemStack = builder.removeFirst();
            if (itemStack != null) {
                SoundUtil.playRemoveOneSound(player);
                cursorStackReference.set(itemStack);
            }
        } else {
            if (!(otherStack.getItem() instanceof ArmorItem otherArmor)) return false;
            ArmorItem armorItem = (ArmorItem) stack.getItem();
            EquipmentSlot armorSlot = armorItem.getSlotType();
            EquipmentSlot otherSlot = otherArmor.getSlotType();
            if (!UnderArmorContentsComponent.isAllowedInUnderArmor(otherStack) || !(armorSlot == otherSlot)) return false;
            int i = builder.add(otherStack);
            if (i > 0) {
                SoundUtil.playInsertSound(player);
            }
        }
        stack.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        UnderArmorContentsComponent underArmorContentsComponent = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (underArmorContentsComponent != null) {
            if (stack.isIn(UNDER_ARMOR)) {
                tooltip.add(Text.translatable("tag.item.progression_respun.under_armor").formatted(Formatting.ITALIC).formatted(Formatting.DARK_GRAY));
            } else if (!stack.isIn(UNDER_ARMOR) && !(stack.isIn(BYPASSES_UNDER_ARMOR))) {
                tooltip.add(Text.translatable("tag.item.progression_respun.needs_under_armor").formatted(Formatting.ITALIC).formatted(Formatting.DARK_GRAY));
            }
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        SoundUtil.playDropContentsSound(entity);
        ItemUsage.spawnItemContents(entity, UnderArmorContentsComponent.DEFAULT.iterate());
    }
}