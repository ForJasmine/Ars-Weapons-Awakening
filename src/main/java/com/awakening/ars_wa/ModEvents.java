package com.awakening.ars_wa;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArsWA.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onMobDrop(LivingDropsEvent event) {
        double chance = Math.random(); // Genera un número entre 0.0 y 1.0

        if (event.getEntity() instanceof EnderMan) {
            if (chance <= 0.15) { // 15% de probabilidad
                event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                    event.getEntity().level(), 
                    event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                    new net.minecraft.world.item.ItemStack(ModItems.FRAGMENTO_DESGASTADO.get())
                ));
            }
        } else {
            if (chance <= 0.02) { // 2% de probabilidad para el resto
                event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                    event.getEntity().level(), 
                    event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                    new net.minecraft.world.item.ItemStack(ModItems.FRAGMENTO_DESGASTADO.get())
                ));
            }
        }
    }
}