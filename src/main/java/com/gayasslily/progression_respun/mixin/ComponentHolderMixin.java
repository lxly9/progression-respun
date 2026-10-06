package com.gayasslily.progression_respun.mixin;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.gayasslily.progression_respun.item.ComponentHolderState.*;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {
    @Shadow ComponentMap getComponents();

    @Shadow
    @Nullable <T> T get(ComponentType<? extends T> type);

    @SuppressWarnings({"ConstantValue", "unchecked"})
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    default <T> void progressionrespun$getForItemStack(ComponentType<? extends T> type, CallbackInfoReturnable<T> cir) {
        if (!((Object) this instanceof ItemStack itemStack)) return;

        boolean isEnchantments = type == DataComponentTypes.ENCHANTMENTS;
        if ((getBlockedBrokenComponents().contains(type) || isEnchantments) && isItemStackBroken(itemStack)) {
            if (isEnchantments && getComponents().contains(DataComponentTypes.ENCHANTMENTS)) {
                ItemEnchantmentsComponent component = (ItemEnchantmentsComponent) cir.getReturnValue();
                ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                if (component != null) {
                    if (component.getEnchantments().stream().anyMatch(entry -> entry.matchesKey(Enchantments.VANISHING_CURSE))) builder.add((RegistryEntry<Enchantment>) Enchantments.VANISHING_CURSE, 1);
                }
                ItemEnchantmentsComponent component1 = builder.build();
                cir.setReturnValue((T) component1);
            }
            cir.setReturnValue(null);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    default <T> void progressionrespun$getOrDefaultForItemStack(ComponentType<? extends T> type, T fallback, CallbackInfoReturnable<T> cir) {
        if (!((Object) this instanceof ItemStack itemStack)) return;

        if ((getBlockedBrokenComponents().contains(type) || type == DataComponentTypes.ENCHANTMENTS) && isItemStackBroken(itemStack)) {
            cir.setReturnValue(fallback);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    default void progressionrespun$containsForItemStack(ComponentType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ItemStack itemStack)) return;

        if (getBlockedBrokenComponents().contains(type) && isItemStackBroken(itemStack)) {
            cir.setReturnValue(false);
        }
    }
}
