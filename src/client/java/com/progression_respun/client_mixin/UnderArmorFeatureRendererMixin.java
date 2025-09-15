package com.progression_respun.client_mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ArmorFeatureRenderer.class)
public abstract class UnderArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Final
    @Shadow
    private SpriteAtlasTexture armorTrimsAtlas;

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void renderUnderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {

        ItemStack vanillaStack = entity.getEquippedStack(slot);
        if (!vanillaStack.isEmpty()) return;

        Optional<ItemStack> underArmor = Optional.empty();

        if (TrinketsApi.getTrinketComponent(entity).isPresent()) {
            var component = TrinketsApi.getTrinketComponent(entity).get();
            Identifier tagId = Identifier.of("trinkets", slot.name().toLowerCase() + "/under_armor_" + slot.name().toLowerCase());

            List<Pair<SlotReference, ItemStack>> equipped = component.getEquipped(
                    stack -> stack.isIn(TagKey.of(RegistryKeys.ITEM, tagId))
            );

            underArmor = equipped.stream()
                    .map(Pair::getRight)
                    .findFirst();
        }

        underArmor.ifPresent(stack -> {
            if (!(stack.getItem() instanceof ArmorItem armorItem)) return;
            if (armorItem.getSlotType() != slot) return;

            ((ArmorFeatureRenderer<T, M, A>) (Object) this).getContextModel()
                    .copyBipedStateTo(model);

            switch (slot) {
                case HEAD -> {
                    model.setVisible(false);
                    model.head.visible = true;
                    model.hat.visible = true;
                }
                case CHEST -> {
                    model.setVisible(false);
                    model.body.visible = true;
                    model.rightArm.visible = true;
                    model.leftArm.visible = true;
                }
                case LEGS -> {
                    model.setVisible(false);
                    model.body.visible = true;
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                }
                case FEET -> {
                    model.setVisible(false);
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                }
            }

            boolean inner = slot == EquipmentSlot.LEGS;

            var material = armorItem.getMaterial();
            var material1 = material.value();
            for (var layer : material1.layers()) {
                int color = layer.isDyeable() && stack.isIn(net.minecraft.registry.tag.ItemTags.DYEABLE)
                        ? net.minecraft.util.math.ColorHelper.Argb.fullAlpha(
                        net.minecraft.component.type.DyedColorComponent.getColor(stack, -6265536))
                        : -1;

                Identifier texture = layer.getTexture(inner);
                model.render(matrices,
                        vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getArmorCutoutNoCull(texture)),
                        light,
                        net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                        color);
            }

            var armorTrim = stack.get(net.minecraft.component.DataComponentTypes.TRIM);
            if (armorTrim != null) {
                var sprite = armorTrimsAtlas.getSprite(inner ? armorTrim.getLeggingsModelId(material) : armorTrim.getGenericModelId(material));
                var vertexConsumer = sprite.getTextureSpecificVertexConsumer(
                        vertexConsumers.getBuffer(
                                net.minecraft.client.render.TexturedRenderLayers.getArmorTrims(
                                        armorTrim.getPattern().value().decal()
                                )
                        )
                );
                model.render(matrices, vertexConsumer, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV);
            }

            if (stack.hasGlint()) {
                model.render(matrices,
                        vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getArmorEntityGlint()),
                        light,
                        net.minecraft.client.render.OverlayTexture.DEFAULT_UV);
            }
        });

        ci.cancel();
    }
}
