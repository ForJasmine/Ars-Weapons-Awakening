package com.awakening.ars_wa;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ArsWA.MODID);

    public static final RegistryObject<BlockEntityType<AparatoDespertarBlockEntity>> APARATO_DESPERTAR_BE =
            BLOCK_ENTITIES.register("aparato_despertar_be", () ->
                    BlockEntityType.Builder.of(AparatoDespertarBlockEntity::new,
                            ModBlocks.APARATO_DESPERTAR.get()).build(null));
}