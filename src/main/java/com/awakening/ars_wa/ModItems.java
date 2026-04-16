package com.awakening.ars_wa;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // Creamos el registro que conectará nuestros ítems con el MODID "ars_wa"
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, ArsWA.MODID);

    // 1. Registro del Fragmento Desgastado
    // Este es el objeto que soltarán los mobs (Endermans 15%, otros 2%)
    public static final RegistryObject<Item> FRAGMENTO_DESGASTADO = ITEMS.register("fragmento_desgastado", 
            () -> new Item(new Item.Properties()));

    // 2. Registro de la Empuñadura Arcana
    // Este es el objeto que se craftea y se usa en el pedestal para el ritual
    public static final RegistryObject<Item> EMPUNADURA_ARCANA = ITEMS.register("empunadura_arcana", 
            () -> new Item(new Item.Properties()));
}