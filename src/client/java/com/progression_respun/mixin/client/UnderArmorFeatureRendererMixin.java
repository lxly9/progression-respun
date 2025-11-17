package com.progression_respun.mixin.client;

import com.progression_respun.component.ModDataComponentTypes;
import com.progression_respun.component.type.UnderArmorContentsComponent;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArmorFeatureRenderer.class)
public abstract class UnderArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Redirect(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack redirectGetEquippedStack(LivingEntity entity, EquipmentSlot slot) {
        ItemStack originalStack = entity.getEquippedStack(slot);

        if (originalStack.getItem() instanceof ArmorItem armorItem) {
            if (!UnderArmorContentsComponent.hasArmorSlot(originalStack)) return originalStack;

            UnderArmorContentsComponent component = originalStack.get(ModDataComponentTypes.UNDER_ARMOR_CONTENTS);
            if (component == null || component.isEmpty()) return originalStack;

            ItemStack armorInside = component.get(0);
            if (armorInside.isEmpty() || !(armorInside.getItem() instanceof ArmorItem)) return originalStack;
            return armorInside;
        }

        return originalStack;
    }
}
