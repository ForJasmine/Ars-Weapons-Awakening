package com.awakening.ars_wa;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 
Clase principal del mod Ars_WA (Weapon Awakening).
Coordina el registro de ítems, eventos y la integración con Ars Nouveau.*/
@Mod(ArsWA.MODID)
public class ArsWA {
    // El MODID debe ser siempre en minúsculas y coincidir con tu archivo mods.toml
    public static final String MODID = "ars_wa";

    public ArsWA() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. Vincula el registro de ítems (Fragmento y Empuñadura) al bus del sistema
        ModItems.ITEMS.register(modEventBus);

        // 2. Registra el método setup para que se ejecute durante la carga inicial
        modEventBus.addListener(this::setup);

        // 3. Registra esta clase en el bus de eventos general de Forge
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // El registro de rituales debe hacerse dentro de enqueueWork para ser seguro
        event.enqueueWork(() -> {
            // Registra la lógica de despertar armas en el sistema de Ars Nouveau
            // Esto conecta el archivo ritual_despertar.java con el juego
            ArsNouveauAPI.getInstance().registerRitual(new ritual_despertar());
        });
    }
}

