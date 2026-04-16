package com.awakening.ars_wa;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class ritual_despertar extends AbstractRitual {

    public static final ResourceLocation RITUAL_ID = new ResourceLocation (ArsWA.MODID, "ritual_despertar");

    public ritual_despertar() {
        super();
    }

    // ERROR 1 RESUELTO: Implementar getRegistryName
    @Override
    public ResourceLocation getRegistryName() {
        return RITUAL_ID;
    }

    // ERROR 2 RESUELTO: Implementar tick (obligatorio aunque esté vacío)
    @Override
    public void tick() {
        // No necesitamos hacer nada durante el proceso, solo al final.
    }

    @Override
    public void onRitualFinished() {
        ItemStack itemEnCentro = tile.getStackInSlot(0);

        if (ModUtils.esArmaValida(itemEnCentro)) {
            CompoundTag nbt = itemEnCentro.getOrCreateTag();
            nbt.putBoolean("ars_wa.awakened", true);
            nbt.putInt("num_spell_slots", 1); 

            tile.consumeItems(); 
        }
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModItems.EMPUNADURA_ARCANA.get());
    }
}