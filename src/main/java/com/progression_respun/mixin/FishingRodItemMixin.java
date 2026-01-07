package com.progression_respun.mixin;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.FishingBaitContentsComponent;
import com.progression_respun.util.SoundUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug(export = true)
@Mixin(FishingRodItem.class)
public class FishingRodItemMixin extends Item {

    public FishingRodItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        int i;
        if (clickType != ClickType.RIGHT) return false;

        FishingBaitContentsComponent component = stack.get(ModDataComponentTypes.FISHING_BAIT);
        if (component == null) return false;
        if (FishingBaitContentsComponent.isAllowedAsBait(stack)) return false;

        ItemStack itemStack = slot.getStack();
        FishingBaitContentsComponent.Builder builder = new FishingBaitContentsComponent.Builder(component);

        if (itemStack.isEmpty()) {
            SoundUtil.playRemoveArmorSound(player);
            ItemStack itemStack2 = builder.removeFirst();
            if (itemStack2 != null) {
                ItemStack itemStack3 = slot.insertStack(itemStack2);
                builder.add(itemStack3);
            }
        } else if (FishingBaitContentsComponent.isAllowedAsBait(itemStack) && itemStack.getItem().canBeNested() && (i = builder.add(slot, player)) > 0) {
            SoundUtil.playInsertArmorSound(player);
        }
        stack.set(ModDataComponentTypes.FISHING_BAIT, builder.build());
        return true;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {

        if (!slot.canTakePartial(player)) return false;

        FishingBaitContentsComponent component = stack.get(ModDataComponentTypes.FISHING_BAIT);
        if (component == null) return false;
        FishingBaitContentsComponent.Builder builder = new FishingBaitContentsComponent.Builder(component);
        if (otherStack.isEmpty() && clickType == ClickType.RIGHT) {
            ItemStack itemStack = builder.removeFirst();
            if (itemStack == null) return false;
            if (itemStack.isDamaged()) return false;
            SoundUtil.playRemoveArmorSound(player);
            cursorStackReference.set(itemStack);
        } else {
            FishingBaitContentsComponent component1 = otherStack.get(ModDataComponentTypes.FISHING_BAIT);
            if (!FishingBaitContentsComponent.isAllowedAsBait(otherStack) || clickType != ClickType.LEFT) return false;
            if (component1 != null && (!component.isEmpty() || !component1.isEmpty())) return false;
            int i = builder.add(otherStack);
            if (i > 0) {
                SoundUtil.playInsertArmorSound(player);
            }
        }
        stack.set(ModDataComponentTypes.FISHING_BAIT, builder.build());
        return true;
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        FishingBaitContentsComponent component = entity.getStack().get(ModDataComponentTypes.FISHING_BAIT);
        if (component == null) return;
        entity.getStack().set(ModDataComponentTypes.FISHING_BAIT, FishingBaitContentsComponent.DEFAULT);
        ItemUsage.spawnItemContents(entity, component.iterateCopy());
    }
}
