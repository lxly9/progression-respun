package com.progression_respun.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.progression_respun.item.ModItems;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.progression_respun.ProgressionRespun.getArmor;
import static com.progression_respun.ProgressionRespun.getBait;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow @Final private ItemModels models;

    @Unique
    private ClientWorld getWorld = null;
    @Unique private LivingEntity getEntity = null;
    @Unique private int getSeed = 0;

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", at = @At("HEAD"))
    private void progressionrespun$getValues(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        this.getEntity = entity;
        this.getWorld = (ClientWorld) world;
        this.getSeed = seed;
    }

    @WrapMethod(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
    private void progressionrespun$swapUnderArmorStack(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, Operation<Void> original) {
        ItemStack armorStack = getArmor(stack);
        ItemStack baitStack = getBait(stack);
        if (armorStack != ItemStack.EMPTY) {
            original.call(armorStack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, models.getModel(armorStack).getOverrides().apply(models.getModel(armorStack), armorStack, getWorld, getEntity, getSeed));
        } else if (baitStack != ItemStack.EMPTY) {
            if (baitStack.isOf(Items.CARROT)) {
                original.call(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, models.getModel(Items.CARROT_ON_A_STICK));
            }
            if (baitStack.isOf(Items.WARPED_FUNGUS)) {
                original.call(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, models.getModel(Items.WARPED_FUNGUS_ON_A_STICK));
            }
        } else {
            original.call(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
        }
    }
}
