package com.progression_respun.component.type;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.data.ModItemTagProvider;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

public final class UnderArmorContentsComponent
implements TooltipData {
    public static final UnderArmorContentsComponent DEFAULT = new UnderArmorContentsComponent(List.of());
    public static final Codec<UnderArmorContentsComponent> CODEC = ItemStack.CODEC.listOf().xmap(UnderArmorContentsComponent::new, component -> component.stacks);
    public static final PacketCodec<RegistryByteBuf, UnderArmorContentsComponent> PACKET_CODEC = ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(UnderArmorContentsComponent::new, component -> component.stacks);
    private static final Fraction NESTED_UNDER_ARMOR_OCCUPANCY = Fraction.getFraction(1, 16);
    private static final int ADD_TO_NEW_SLOT = -1;
    final List<ItemStack> stacks;
    final Fraction occupancy;

    public static boolean isAllowedInUnderArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public static boolean hasArmorSlot(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && stack.isIn(ModItemTagProvider.UNDER_ARMOR);
    }

    UnderArmorContentsComponent(List<ItemStack> stacks, Fraction occupancy) {
        this.stacks = stacks;
        this.occupancy = occupancy;
    }

    public UnderArmorContentsComponent(List<ItemStack> stacks) {
        this(stacks, UnderArmorContentsComponent.calculateOccupancy(stacks));
    }

    private static Fraction calculateOccupancy(List<ItemStack> stacks) {
        Fraction fraction = Fraction.ZERO;
        for (ItemStack itemStack : stacks) {
            fraction = fraction.add(UnderArmorContentsComponent.getOccupancy(itemStack).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
        }
        return fraction;
    }

    public static Fraction getOccupancy(ItemStack stack) {
        UnderArmorContentsComponent underArmorContentsComponent = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (underArmorContentsComponent != null) {
            return NESTED_UNDER_ARMOR_OCCUPANCY.add(underArmorContentsComponent.getOccupancy());
        }
        List list = stack.getOrDefault(DataComponentTypes.BEES, List.of());
        if (!list.isEmpty()) {
            return Fraction.ONE;
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
        return Lists.transform(this.stacks, ItemStack::copy);
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
        if (o instanceof UnderArmorContentsComponent) {
            UnderArmorContentsComponent underArmorContentsComponent = (UnderArmorContentsComponent)o;
            return this.occupancy.equals(underArmorContentsComponent.occupancy) && ItemStack.stacksEqual(this.stacks, underArmorContentsComponent.stacks);
        }
        return false;
    }

    public int hashCode() {
        return ItemStack.listHashCode(this.stacks);
    }

    public String toString() {
        return "Armor" + String.valueOf(this.stacks);
    }

    public static void dropAllBundledItems(ItemStack stack2, PlayerEntity player) {
        BundleContentsComponent bundleContentsComponent = stack2.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null || bundleContentsComponent.isEmpty()) {
            return;
        }
        stack2.set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        if (player instanceof ServerPlayerEntity) {
            bundleContentsComponent.iterateCopy().forEach(stack -> player.dropItem((ItemStack)stack, true));
        }
    }

    public static class Builder {
        private final List<ItemStack> stacks;
        private Fraction occupancy;

        public Builder(UnderArmorContentsComponent base) {
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
            return Math.max(fraction.divideBy(UnderArmorContentsComponent.getOccupancy(stack)).intValue(), 0);
        }

        public int add(ItemStack stack) {
            if (stack.isEmpty() || !stack.getItem().canBeNested()) {
                return 0;
            }
            int i = Math.min(stack.getCount(), this.getMaxAllowed(stack));
            if (i == 0) {
                return 0;
            }
            this.occupancy = this.occupancy.add(UnderArmorContentsComponent.getOccupancy(stack).multiplyBy(Fraction.getFraction(i, 1)));
            int j = this.addInternal(stack);
            if (j != -1) {
                ItemStack itemStack = this.stacks.remove(j);
                ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + i);
                stack.decrement(i);
                this.stacks.add(0, itemStack2);
            } else {
                this.stacks.add(0, stack.split(i));
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
            ItemStack itemStack = this.stacks.remove(0).copy();
            this.occupancy = this.occupancy.subtract(UnderArmorContentsComponent.getOccupancy(itemStack).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
            return itemStack;
        }

        public Fraction getOccupancy() {
            return this.occupancy;
        }

        public UnderArmorContentsComponent build() {
            return new UnderArmorContentsComponent(List.copyOf(this.stacks), this.occupancy);
        }
    }
}

