package com.progression_respun.mixin.fishing_rod;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.FishingBaitContentsComponent;
import com.progression_respun.item.ModItems;
import com.progression_respun.util.SoundUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.Stats;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

import static com.progression_respun.ProgressionRespun.getBait;

@Debug(export = true)
@Mixin(FishingRodItem.class)
public class FishingRodItemMixin<T extends Entity & ItemSteerable> extends Item {

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

    @WrapMethod(method = "use")
    private TypedActionResult<ItemStack> progressionrespun$boostWithRod(World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack baitStack = getBait(itemStack);
        if (baitStack != ItemStack.EMPTY && baitStack.getItem() != ModItems.WORM) {
            if (world.isClient) {
                return TypedActionResult.pass(itemStack);
            } else {
                Entity entity = user.getControllingVehicle();
                if (user.hasVehicle() && entity instanceof ItemSteerable itemSteerable && itemSteerable.consumeOnAStickItem()) {
                    baitStack.set(DataComponentTypes.DAMAGE, baitStack.getDamage() + 1);
                    return TypedActionResult.success(itemStack);
                } else {
                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    return TypedActionResult.pass(itemStack);
                }
            }
        }
        return original.call(world, user, hand);
    }
}
