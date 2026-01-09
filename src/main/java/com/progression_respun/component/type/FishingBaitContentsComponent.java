package com.progression_respun.component.type;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.progression_respun.component.ModDataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.slot.Slot;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.progression_respun.data.ModItemTagProvider.BAIT;

public final class FishingBaitContentsComponent
implements TooltipData {
    public static final FishingBaitContentsComponent DEFAULT = new FishingBaitContentsComponent(List.of());
    public static final Codec<FishingBaitContentsComponent> CODEC = ItemStack.CODEC.listOf().xmap(FishingBaitContentsComponent::new, component -> component.stacks);
    public static final PacketCodec<RegistryByteBuf, FishingBaitContentsComponent> PACKET_CODEC = ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(FishingBaitContentsComponent::new, component -> component.stacks);
    private static final Fraction NESTED_FISHING_BAIT_OCCUPANCY = Fraction.ONE;
    private static final int ADD_TO_NEW_SLOT = -1;
    final List<ItemStack> stacks;
    final Fraction occupancy;

    public static boolean isAllowedAsBait(ItemStack stack) {
        return stack.isIn(BAIT);
    }

    FishingBaitContentsComponent(List<ItemStack> stacks, Fraction occupancy) {
        this.stacks = stacks;
        this.occupancy = occupancy;
    }

    public FishingBaitContentsComponent(List<ItemStack> stacks) {
        this(stacks, FishingBaitContentsComponent.calculateOccupancy(stacks));
    }

    public static float getAmountFilled(ItemStack stack) {
        FishingBaitContentsComponent component = stack.getOrDefault(ModDataComponentTypes.FISHING_BAIT, FishingBaitContentsComponent.DEFAULT);
        return component.getOccupancy().floatValue();
    }

    private static Fraction calculateOccupancy(List<ItemStack> stacks) {
        Fraction fraction = Fraction.ZERO;
        for (ItemStack itemStack : stacks) {
            fraction = fraction.add(FishingBaitContentsComponent.getOccupancy(itemStack).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
        }
        return fraction;
    }


    public static Fraction getOccupancy(ItemStack stack) {
        FishingBaitContentsComponent fishingBaitContentsComponent = stack.get(ModDataComponentTypes.FISHING_BAIT);
        if (fishingBaitContentsComponent != null) {
            return NESTED_FISHING_BAIT_OCCUPANCY.add(fishingBaitContentsComponent.getOccupancy());
        }
        return Fraction.getFraction(1, stack.getMaxCount());
    }

    public ItemStack get(int index) {
        return this.stacks.get(index);
    }

    public Stream<ItemStack> stream() {
        return this.stacks.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> iterate() {
        return this.stacks;
    }

    public Iterable<ItemStack> iterateCopy() {
        return Lists.transform(this.stacks, stack -> stack != null ? stack.copy() : null);
    }

    public int size() {
        return this.stacks.size();
    }

    public Fraction getOccupancy() {
        return this.occupancy;
    }

    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FishingBaitContentsComponent fishingBaitContentsComponent) {
            return this.occupancy.equals(fishingBaitContentsComponent.occupancy) && ItemStack.stacksEqual(this.stacks, fishingBaitContentsComponent.stacks);
        }
        return false;
    }

    public int hashCode() {
        return ItemStack.listHashCode(this.stacks);
    }

    public String toString() {
        return "Bait" + String.valueOf(this.stacks);
    }

    public static class Builder {
        private final List<ItemStack> stacks;
        private Fraction occupancy;

        public Builder(FishingBaitContentsComponent base) {
            this.stacks = new ArrayList<ItemStack>(base.stacks);
            this.occupancy = base.occupancy;
        }

        public Builder clear() {
            this.stacks.clear();
            this.occupancy = Fraction.ZERO;
            return this;
        }

        private int addInternal(ItemStack stack) {
            if (!stack.isStackable()) {
                return -1;
            }
            for (int i = 0; i < this.stacks.size(); ++i) {
                if (!ItemStack.areItemsAndComponentsEqual(this.stacks.get(i), stack)) continue;
                return i;
            }
            return -1;
        }

        private int getMaxAllowed(ItemStack stack) {
            Fraction fraction = Fraction.ONE.subtract(this.occupancy);
            return Math.max(fraction.divideBy(FishingBaitContentsComponent.getOccupancy(stack)).intValue(), 0);
        }

        public int add(ItemStack stack) {
            if (stack.isEmpty() || !stack.getItem().canBeNested()) {
                return 0;
            }
            int i = Math.min(stack.getCount(), this.getMaxAllowed(stack));
            if (i == 0) {
                return 0;
            }
            this.occupancy = this.occupancy.add(FishingBaitContentsComponent.getOccupancy(stack).multiplyBy(Fraction.getFraction(i, 1)));
            int j = this.addInternal(stack);
            if (j != -1) {
                ItemStack itemStack = this.stacks.remove(j);
                ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + i);
                stack.decrement(i);
                this.stacks.addFirst(itemStack2);
            } else {
                this.stacks.addFirst(stack.split(i));
            }
            return i;
        }

        public int add(Slot slot, PlayerEntity player) {
            ItemStack itemStack = slot.getStack();
            int i = this.getMaxAllowed(itemStack);
            return this.add(slot.takeStackRange(itemStack.getCount(), i, player));
        }

        @Nullable
        public ItemStack removeFirst() {
            if (this.stacks.isEmpty()) {
                return null;
            }
            ItemStack itemStack = this.stacks.removeFirst().copy();
            this.occupancy = this.occupancy.subtract(FishingBaitContentsComponent.getOccupancy(itemStack).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
            return itemStack;
        }

        public Fraction getOccupancy() {
            return this.occupancy;
        }

        public FishingBaitContentsComponent build() {
            return new FishingBaitContentsComponent(List.copyOf(this.stacks), this.occupancy);
        }
    }
}

