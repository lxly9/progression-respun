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
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
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

import static com.progression_respun.ProgressionRespun.hasBinding;
import static com.progression_respun.data.ModItemTagProvider.BYPASSES_UNDER_ARMOR;
import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;equipAndSwap(Lnet/minecraft/item/Item;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"), cancellable = true)
    private void equipAndSwap(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof ArmorItem armorItem) {
            EquipmentSlot slot = armorItem.getSlotType();
            ItemStack underArmor = player.getEquippedStack(slot);
            if (stack.isIn(UNDER_ARMOR)) {
                if (!underArmor.isEmpty()) {
                    float armorOccupancy = UnderArmorContentsComponent.getAmountFilled(underArmor);
                    if (armorOccupancy <= 0) {
                        float stackOccupancy = UnderArmorContentsComponent.getAmountFilled(stack);
                        if (stackOccupancy <= 0) {
                            UnderArmorContentsComponent stackComponent = underArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                            if (stackComponent != null) {
                                UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(stackComponent);
                                builder.add(stack);
                                underArmor.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
                                cir.setReturnValue(TypedActionResult.success(stack));
                            }
                        }
                    }
                }
            } else if (!stack.isIn(BYPASSES_UNDER_ARMOR)) {
                if (!underArmor.isEmpty()) {
                    float armorOccupancy = UnderArmorContentsComponent.getAmountFilled(underArmor);
                    if (armorOccupancy <= 0) {
                        UnderArmorContentsComponent stackComponent = underArmor.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                        if (stackComponent != null) {
                            UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(stackComponent);
                            builder.add(stack);
                            underArmor.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
                            cir.setReturnValue(TypedActionResult.success(stack));
                        }
                    }
                    cir.setReturnValue(TypedActionResult.fail(stack));
                }
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        }
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
        UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(component);

        ArmorItem armorItem = (ArmorItem) stack.getItem();

        if (itemStack.isEmpty() && !hasBinding(itemStack)) {
            SoundUtil.playRemoveArmorSound(player);
            ItemStack itemStack2 = builder.removeFirst();
            if (itemStack2 != null) {
                ItemStack itemStack3 = slot.insertStack(itemStack2);
                builder.add(itemStack3);
            }
        } else if (itemStack.getItem() instanceof ArmorItem otherArmor && !(armorItem.getSlotType() == otherArmor.getSlotType())) {
            return false;
        } else if (itemStack.getItem() instanceof ElytraItem) {
            return false;
        } else if (UnderArmorContentsComponent.isAllowedInUnderArmor(itemStack) && itemStack.getItem().canBeNested() && (i = builder.add(slot, player)) > 0) {
            SoundUtil.playInsertArmorSound(player);
        }
        stack.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
        return true;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {

        if (!slot.canTakePartial(player)) return false;
        if (!UnderArmorContentsComponent.hasArmorSlot(stack)) return false;

        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component == null) return false;
        UnderArmorContentsComponent.Builder builder = new UnderArmorContentsComponent.Builder(component);
        if (otherStack.isEmpty() && clickType == ClickType.RIGHT && !hasBinding(stack) && !hasBinding(otherStack)) {
            ItemStack itemStack = builder.removeFirst();
            if (itemStack != null) {
                SoundUtil.playRemoveArmorSound(player);
                cursorStackReference.set(itemStack);
            }
        } else {
            if (!(otherStack.getItem() instanceof ArmorItem otherArmor)) return false;
            UnderArmorContentsComponent component1 = otherStack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            ArmorItem armorItem = (ArmorItem) stack.getItem();
            EquipmentSlot armorSlot = armorItem.getSlotType();
            EquipmentSlot otherSlot = otherArmor.getSlotType();
            if (!UnderArmorContentsComponent.isAllowedInUnderArmor(otherStack) || !(armorSlot == otherSlot) || clickType != ClickType.LEFT) return false;
            if (component1 != null && (!component.isEmpty() || !component1.isEmpty())) return false;
            int i = builder.add(otherStack);
            if (i > 0) {
                SoundUtil.playInsertArmorSound(player);
            }
        }
        stack.set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, builder.build());
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component != null) {
            if (stack.isIn(UNDER_ARMOR)) {
                boolean hasArmor = UnderArmorContentsComponent.getAmountFilled(stack) > 0;
                if (!hasArmor) {
                    tooltip.add(Text.translatable("tag.item.progression_respun.under_armor").formatted(Formatting.GRAY));
                    tooltip.add(Text.translatable("tag.item.progression_respun.equip_armor").formatted(Formatting.ITALIC).formatted(Formatting.DARK_GRAY));
                } else {
                    tooltip.add(Text.translatable("tag.item.progression_respun.unequip_under_armor").formatted(Formatting.ITALIC).formatted(Formatting.DARK_GRAY));
                }
            } else if (!stack.isIn(UNDER_ARMOR) && !(stack.isIn(BYPASSES_UNDER_ARMOR))) {
                tooltip.add(Text.translatable("tag.item.progression_respun.needs_under_armor").formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("tag.item.progression_respun.equip_under_armor").formatted(Formatting.ITALIC).formatted(Formatting.DARK_GRAY));
            }
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        UnderArmorContentsComponent component = entity.getStack().get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component == null) return;
        entity.getStack().set(ModDataComponentTypes.UNDER_ARMOR_CONTENTS, UnderArmorContentsComponent.DEFAULT);
        ItemUsage.spawnItemContents(entity, component.iterateCopy());
    }
}