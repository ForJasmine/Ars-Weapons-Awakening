package com.awakening.ars_wa;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ModUtils {
    public static boolean esArmaValida(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        
        // 1. Mantenemos la detección original para armas vanilla y herramientas.
        boolean esArmaVanilla = item instanceof TieredItem || 
                                item instanceof ProjectileWeaponItem || 
                                item instanceof TridentItem;

        // 2. Añadimos una detección universal para armas de mods.
        //    Esto comprueba si el ítem tiene un atributo de daño de ataque en la mano principal.
        boolean tieneAtributoDeDano = !stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).isEmpty();

        return esArmaVanilla || tieneAtributoDeDano;
    }
}