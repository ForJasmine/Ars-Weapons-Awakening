package com.awakening.ars_wa;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Esta clase solo se carga en el lado del cliente (Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ArsWA.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Conectamos tu BlockEntity con el Renderizador que acabamos de crear
        event.registerBlockEntityRenderer(ModBlockEntities.APARATO_DESPERTAR_BE.get(), AparatoDespertarRenderer::new);
    }
}