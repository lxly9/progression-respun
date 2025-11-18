package com.progression_respun.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.data.ModItemTagProvider.UNDER_ARMOR;

@Debug(export = true)
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow @Final private ItemModels models;

    @Unique
    private ClientWorld getWorld = null;
    @Unique private LivingEntity getEntity = null;
    @Unique private int getSeed = 0;

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", at = @At("HEAD"))
    private void bountifulFares$getValues(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        this.getEntity = entity;
        this.getWorld = (ClientWorld) world;
        this.getSeed = seed;
    }

    @WrapMethod(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
    private void swapUnderArmorStack(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, Operation<Void> original) {
        if (!stack.isEmpty() && stack.isIn(UNDER_ARMOR) && stack.getItem() instanceof ArmorItem) {
            UnderArmorContentsComponent component = stack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component != null && !component.isEmpty()) {
                ItemStack armorStack = component.get(0);
                if (armorStack.getItem() instanceof ArmorItem) {
                    original.call(armorStack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, models.getModel(armorStack).getOverrides().apply(models.getModel(armorStack), armorStack, getWorld, getEntity, getSeed));
                }
            } else {
                original.call(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
            }
        } else {
            original.call(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
        }
    }
}
