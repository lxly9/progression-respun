package com.progression_respun.mixin.underArmorHandling;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Invoker("insertItem")
    boolean invokeInsertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast);
}