//package com.progression_respun.util;
//
//import com.progression_respun.component.ModDataComponentTypes;
//import com.progression_respun.component.type.UnderArmorContentsComponent;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.item.ItemRenderer;
//import net.minecraft.client.render.model.BakedModel;
//import net.minecraft.client.render.model.json.ModelTransformationMode;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.item.ArmorItem;
//import net.minecraft.item.ItemStack;
//
//import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;
//
//public class ModelUtil {
//    public void renderCustomDye(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        ItemRenderer itemRenderer = client.getItemRenderer();
//        boolean leftHanded = mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
//
//        BakedModel underArmorModel = client.getItemRenderer().getModels().getModel(stack);
//
//        if (stack.isIn(UNDER_ARMOR) && stack.getItem() instanceof ArmorItem) {
//            if (UnderArmorContentsComponent.hasArmorSlot(stack)) {
//                float occupancy = UnderArmorContentsComponent.getAmountFilled(stack);
//                if (occupancy > 0) {
//                    UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
//                    if (component != null) {
//                        ItemStack armorStack = component.get(0);
//
//                        BakedModel ArmorModel = client.getItemRenderer().getModels().getModel(armorStack);
//
//
//                        if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem) {
//
//                            matrices.push();
//                            matrices.translate(0.5, 0.5, 0.5);
//                            itemRenderer.renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, underArmorModel);
//                            matrices.pop();
//
//                            matrices.push();
//                            matrices.translate(0.5, 0.5, 0.5);
//                            itemRenderer.renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, ArmorModel);
//                            matrices.pop();
//                        }
//                    }
//                } else {
//                    itemRenderer.renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, underArmorModel);
//                }
//            }
//        }
//        matrices.pop();
//    }
//}
