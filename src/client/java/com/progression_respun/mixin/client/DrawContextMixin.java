package com.progression_respun.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow public abstract void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemBarVisible()Z"))
    private void renderArmorDurabilityBar(net.minecraft.client.font.TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (!UnderArmorContentsComponent.hasArmorSlot(stack)) return;

        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component != null && !component.isEmpty())
        {
            ItemStack armorInside = component.get(0);
            if (armorInside.isEmpty() || !(armorInside.getItem() instanceof ArmorItem)) return;
            if (armorInside.getDamage() == 0) return;

            RenderSystem.disableDepthTest();
            int i = armorInside.getItemBarStep();
            int j = armorInside.getItemBarColor();
            int drawX = x + 2;
            int drawY = y + 11;
            this.fill(RenderLayer.getGuiOverlay(), drawX, drawY, drawX + 13, drawY + 2, -16777216);
            this.fill(RenderLayer.getGuiOverlay(), drawX, drawY, drawX + i, drawY + 1, j | -16777216);
            RenderSystem.enableDepthTest();
        }
    }
}