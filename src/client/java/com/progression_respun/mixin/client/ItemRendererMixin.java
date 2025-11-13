package com.progression_respun.mixin.client;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow protected abstract void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices);

    @Redirect(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"))
    private void renderArmorItem(ItemRenderer instance, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, ItemStack stack1, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices1, VertexConsumerProvider vertexConsumers, int light1, int overlay1, BakedModel model1) {
        if (stack.isIn(UNDER_ARMOR) && stack.getItem() instanceof ArmorItem) {
            if (UnderArmorContentsComponent.hasArmorSlot(stack)) {
                float occupancy = UnderArmorContentsComponent.getAmountFilled(stack);
                if (occupancy > 0) {
                    UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
                    if (component != null) {
                        ItemStack armorStack = component.get(0);
                        if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem) {
                            this.renderBakedItemModel(instance.getModels().getModel(armorStack), armorStack, light, overlay, matrices, vertices);
                        }
                    }
                } else {
                    this.renderBakedItemModel(model, stack, light, overlay, matrices, vertices);
                }
            }
        } else {
            this.renderBakedItemModel(model, stack, light, overlay, matrices, vertices);
        }
    }
}
