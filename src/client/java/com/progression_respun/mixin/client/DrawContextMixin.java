package com.progression_respun.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Debug(export = true)
@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow public abstract void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color);


    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void drawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y);

    @Shadow public abstract MatrixStack getMatrices();

    @Shadow @Final private MatrixStack matrices;

    @Shadow public abstract VertexConsumerProvider.Immediate getVertexConsumers();

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemBarVisible()Z"))
    private void renderArmorDurabilityBar(net.minecraft.client.font.TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (!UnderArmorContentsComponent.hasArmorSlot(stack)) return;
        float occupancy = UnderArmorContentsComponent.getAmountFilled(stack);
        if (occupancy <= 0) return;

        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component == null) return;

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

    @Inject(method = "drawItemTooltip", at = @At("HEAD"), cancellable = true)
    private void drawArmorTooltip(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (UnderArmorContentsComponent.hasArmorSlot(stack)) return;
        float occupancy = UnderArmorContentsComponent.getAmountFilled(stack);
        if (occupancy <= 0) return;

        UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
        if (component == null) return;

        ItemStack armorInside = component.get(0);
        drawTooltip(textRenderer, Screen.getTooltipFromItem(client, armorInside), armorInside.getTooltipData(), x, y);
        ci.cancel();
    }

//    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At(value = "HEAD"))
//    private void renderUnderAndArmor(LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
//        if (!stack.isEmpty() && stack.isIn(UNDER_ARMOR) && stack.getItem() instanceof ArmorItem armorItem) {
//            if (!UnderArmorContentsComponent.hasArmorSlot(stack)) return;
//            float occupancy = UnderArmorContentsComponent.getAmountFilled(stack);
//            if (occupancy <= 0) return;
//
//            UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
//            if (component == null) return;
//
//            ItemStack armorInside = component.get(0);
//            if (armorInside.isEmpty() || !(armorInside.getItem() instanceof ArmorItem)) return;
//
//            BakedModel bakedModel = this.client.getItemRenderer().getModel(stack, world, entity, seed);
//            this.matrices.push();
//            this.matrices.translate((float)(x + 8), (float)(y + 8), (float)(150 + (bakedModel.hasDepth() ? z : 0)));
//
//            BakedModel bakedModel2 = this.client.getItemRenderer().getModel(stack, world, entity, seed);
//            this.matrices.push();
//            this.matrices.translate((float)(x + 8), (float)(y + 8), (float)(150 + (bakedModel.hasDepth() ? z : 1)));
//
//            if (entity instanceof PlayerEntity player){
//                client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, this.matrices, this.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
//                client.getItemRenderer().renderItem(armorInside, ModelTransformationMode.GUI, false, this.matrices, this.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel2);
//            }
//        }
//    }
}