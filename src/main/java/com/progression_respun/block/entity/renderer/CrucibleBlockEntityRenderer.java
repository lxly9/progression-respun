//package com.progression_respun.block.entity.renderer;
//
//import com.progression_respun.block.entity.CrucibleBlockEntity;
//import net.minecraft.client.render.LightmapTextureManager;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.block.entity.BlockEntityRenderer;
//import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
//import net.minecraft.client.render.item.ItemRenderer;
//import net.minecraft.client.render.model.json.ModelTransformationMode;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.collection.DefaultedList;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.math.RotationAxis;
//import net.minecraft.world.LightType;
//import net.minecraft.world.World;
//
//public class CrucibleBlockEntityRenderer implements BlockEntityRenderer<CrucibleBlockEntity>{
//    private final ItemRenderer itemRenderer;
//
//    public CrucibleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
//        this.itemRenderer = context.getItemRenderer();
//
//    }
//    @Override
//    public void render(CrucibleBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
//        DefaultedList<ItemStack> defaultedList = CrucibleBlockEntity.getItemsBeingCooked();
//        int k = (int)CrucibleBlockEntity.getPos().asLong();
//
//        for(int l = 0; l < defaultedList.size(); ++l) {
//            ItemStack itemStack = (ItemStack)defaultedList.get(l);
//            if (itemStack != ItemStack.EMPTY) {
//                matrices.push();
//                matrices.translate(0.5F, 0.44921875F, 0.5F);
//                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45F));
//                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
//                matrices.translate(-0.3125F, -0.3125F, 0.0F);
//                matrices.scale(0.375F, 0.375F, 0.375F);
//                this.itemRenderer.renderItem(itemStack, ModelTransformationMode.FIXED, i, j, matrices, vertexConsumers, CrucibleBlockEntity.getWorld(), k + l);
//                matrices.pop();
//            }
//        }
//    }
//
//    private int getLightLevel(World world, BlockPos pos) {
//        int bLight = world.getLightLevel(LightType.BLOCK, pos);
//        int sLight = world.getLightLevel(LightType.SKY, pos);
//        return LightmapTextureManager.pack(bLight, sLight);
//    }
//}
