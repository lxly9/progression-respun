package com.progression_respun.mixin;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.progression_respun.item.ComponentHolderState.*;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {
    @Shadow ComponentMap getComponents();

    @SuppressWarnings({"ConstantValue", "unchecked"})
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    default <T> void getForItemStack(ComponentType<? extends T> type, CallbackInfoReturnable<T> cir) {
        if (!((Object) this instanceof ItemStack itemStack)) return;

        boolean isEnchantments = type == DataComponentTypes.ENCHANTMENTS;
        if ((getBlockedBrokenComponents().contains(type) || isEnchantments) && isItemStackBroken(itemStack)) {
            if (isEnchantments && getComponents().contains(DataComponentTypes.ENCHANTMENTS)) {
                cir.setReturnValue((T) ItemEnchantmentsComponent.DEFAULT);
            }
            cir.setReturnValue(null);
        }
//        boolean isStoredEnchantments = type == DataComponentTypes.STORED_ENCHANTMENTS;
//        if (itemStack.getItem() instanceof EnchantedBookItem && isStoredEnchantments && getComponents().contains(DataComponentTypes.STORED_ENCHANTMENTS)) {
//            var opt = itemStack.getEnchantments().getEnchantmentEntries().stream().findFirst();
//
//            if (opt.isPresent()) {
//                var entry = opt.get();
//
//                ItemEnchantmentsComponent enchantmentsComponent = itemStack.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);
//                ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantmentsComponent);
//                builder.add(entry.getKey(), entry.getIntValue());
//                ItemEnchantmentsComponent builtComponent = builder.build();
//
//                cir.setReturnValue((T) builtComponent);
//            }
//        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    default <T> void getOrDefaultForItemStack(ComponentType<? extends T> type, T fallback, CallbackInfoReturnable<T> cir) {
        if (!((Object) this instanceof ItemStack itemStack)) return;

        if ((getBlockedBrokenComponents().contains(type) || type == DataComponentTypes.ENCHANTMENTS) && isItemStackBroken(itemStack)) {
            cir.setReturnValue(fallback);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    default void containsForItemStack(ComponentType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ItemStack itemStack)) return;

        if (getBlockedBrokenComponents().contains(type) && isItemStackBroken(itemStack)) {
            cir.setReturnValue(false);
        }
    }
}
