package com.awakening.ars_wa;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArsWA.MODID)
public class ArsWA {
    public static final String MODID = "ars_wa";

    public ArsWA(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 1. Registro de tus ítems (Fragmento, Empuñadura)
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // 2. Agregamos el evento para insertar nuestros ítems en el modo creativo
        modEventBus.addListener(this::addCreative);
        
        // NOTA: No registramos 'ritual_despertar' aquí porque ahora es una Receta 
        // que el juego cargará vía JSON.
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.FRAGMENTO_DESGASTADO.get());
            event.accept(ModItems.EMPUNADURA_ARCANA.get());
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.APARATO_DESPERTAR.get());
        }
    }
}
