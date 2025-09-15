package com.progression_respun.util;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.util.Identifier;

@Mixin(ArmorFeatureRenderer.class)
public interface ArmorFeatureRendererInvoker<T extends net.minecraft.entity.LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Invoker("setVisible")
    void invokeSetVisible(A model, EquipmentSlot slot);

    @Invoker("usesInnerModel")
    boolean invokeUsesInnerModel(EquipmentSlot slot);

    @Invoker("renderArmorParts")
    void invokeRenderArmorParts(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, int color, Identifier texture);

    @Invoker("renderTrim")
    void invokeRenderTrim(RegistryEntry<net.minecraft.item.ArmorMaterial> material, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, A model, boolean leggings);

    @Invoker("renderGlint")
    void invokeRenderGlint(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model);

    @Invoker("getModel")
    A invokeGetModel(EquipmentSlot slot);
}
