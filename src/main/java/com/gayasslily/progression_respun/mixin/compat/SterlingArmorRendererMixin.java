//package com.progression_respun.mixin.compat;
//
//import com.llamalad7.mixinextras.injector.ModifyReturnValue;
//import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.client.render.VertexFormat;
//import net.minecraft.client.render.entity.model.EntityModel;
//import net.minecraft.entity.LivingEntity;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Pseudo;
//import org.spongepowered.asm.mixin.injection.At;
//
//@Pseudo
//@Mixin(targets = "net.orcinus.galosphere.client.renderer.SterlingArmorRenderer", remap = false)
//public class SterlingArmorRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer {
//    public SterlingArmorRendererMixin(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
//        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
//    }
//
//    @ModifyReturnValue(method = "render", at = @At("S"))
//    private void render() {
//
//    }
//}
