package com.awakening.ars_wa;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AparatoDespertarRenderer implements BlockEntityRenderer<AparatoDespertarBlockEntity> {

    public AparatoDespertarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AparatoDespertarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack itemStack = blockEntity.getStoredItem();
        
        if (!itemStack.isEmpty()) {
            poseStack.pushPose();
            
            // 1. Centrar el ítem y subirlo por encima del bloque (Y = 1.2)
            poseStack.translate(0.5D, 1.2D, 0.5D);
            
            // 2. Hacer que gire suavemente con el tiempo
            long time = blockEntity.getLevel().getGameTime();
            float angle = (time + partialTick) * 4.0F; // Multiplicador de velocidad
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            
            // 3. Renderizar el ítem en el mundo
            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
            
            poseStack.popPose();
        }
    }
}