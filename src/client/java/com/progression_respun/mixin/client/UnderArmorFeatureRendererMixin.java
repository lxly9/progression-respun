package com.progression_respun.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.progression_respun.ProgressionRespun;
import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

import static com.progression_respun.ProgressionRespun.getExternalItem;
import static com.progression_respun.ProgressionRespun.isModLoaded;

@Mixin(ArmorFeatureRenderer.class)
public abstract class UnderArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Redirect(method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectGetEquippedStack(LivingEntity entity, EquipmentSlot slot, @Local(argsOnly = true) MatrixStack matrices, @Local(argsOnly = true) VertexConsumerProvider vertexConsumers) throws ClassNotFoundException, NoSuchMethodException {
        ItemStack originalStack = entity.getEquippedStack(slot);

        if (originalStack.getItem() instanceof ArmorItem armorItem) {
            if (!UnderArmorContentsComponent.hasArmorSlot(originalStack)) return originalStack;

            UnderArmorContentsComponent component = originalStack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component == null || component.isEmpty()) return originalStack;

            ItemStack armorInside = component.get(0);
            if (armorInside.isEmpty() || !(armorInside.getItem() instanceof ArmorItem)) return originalStack;
            if (isModLoaded("galosphere") && armorInside.isOf(getExternalItem("galosphere", "sterling_helmet"))) {
//                Class<?> aClass = Class.forName("net.orcinus.galosphere.client.renderer.SterlingArmorRenderer");
//                Class<?> aClass1 = Class.forName("net.orcinus.galosphere.client.renderer.layer.BannerLayer");
//                Method method = aClass.getMethod("render");
//                Method method1 = aClass.getMethod("render");
            }
            return armorInside;
        }

        return originalStack;
    }
}
