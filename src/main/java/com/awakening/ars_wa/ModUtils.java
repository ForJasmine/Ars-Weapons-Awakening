package com.awakening.ars_wa;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModUtils {

    public static boolean esArmaValida(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 1. Evaluación para armas cuerpo a cuerpo (Espadas, Hachas de mods, etc.)
        // Usamos Sharpness (Filo)
        boolean esMelee = stack.canApplyAtEnchantingTable(Enchantments.SHARPNESS);

        // 2. Evaluación para Arcos y Ballestas
        // Usamos Power (Poder) para arcos y Piercing (Perforación) para ballestas
        boolean esRango = stack.canApplyAtEnchantingTable(Enchantments.POWER_ARROWS);  
                          stack.canApplyAtEnchantingTable(Enchantments.PIERCING);

        // 3. Evaluación para Tridentes
        // Usamos Riptide (Propulsión acuática)
        boolean esTridente = stack.canApplyAtEnchantingTable(Enchantments.RIPTIDE);

        // Si cumple cualquiera de las tres, el arma es apta para el despertar
        return esMelee ||  esRango || esTridente;
    }
}