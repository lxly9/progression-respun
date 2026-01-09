package com.progression_respun.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.ProgressionRespun.getArmor;
import static com.progression_respun.ProgressionRespun.getBait;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {


    @Unique
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);

    @Shadow public abstract void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemBarVisible()Z"))
    private void progressionrespun$renderArmorDurabilityBar(net.minecraft.client.font.TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        ItemStack armorStack = getArmor(stack);
        ItemStack baitStack = getBait(stack);

        if (armorStack != ItemStack.EMPTY && armorStack.isDamaged()) {
            RenderSystem.disableDepthTest();
            int i = armorStack.getItemBarStep();
            int j = armorStack.getItemBarColor();
            int drawX = x + 2;
            int drawY = y + 11;
            this.fill(RenderLayer.getGuiOverlay(), drawX, drawY, drawX + 13, drawY + 2, -16777216);
            this.fill(RenderLayer.getGuiOverlay(), drawX, drawY, drawX + i, drawY + 1, j | -16777216);
            RenderSystem.enableDepthTest();
        }
//        if (baitStack != ItemStack.EMPTY && baitStack.isDamaged()) {
//            RenderSystem.disableDepthTest();
//            int i = baitStack.getItemBarStep();
//            int drawX = x + 2;
//            int drawY = y + 11;
//            this.fill(RenderLayer.getGuiOverlay(), drawX, drawY, drawX + 13, drawY + 2, -16777216);
//            this.fill(RenderLayer.getGuiOverlay(), drawX, drawY, drawX + i, drawY + 1, ITEM_BAR_COLOR | -16777216);
//            RenderSystem.enableDepthTest();
//        }
    }
}