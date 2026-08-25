package com.awakening.ars_wa;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AparatoDespertarBlockEntity extends BlockEntity {
    // Aquí guardamos el arma que el jugador coloque
    private ItemStack storedItem = ItemStack.EMPTY;

    public AparatoDespertarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.APARATO_DESPERTAR_BE.get(), pos, state);
    }
    
    public ItemStack getStoredItem() {
        return storedItem;
    }

    public void setStoredItem(ItemStack item) {
        this.storedItem = item;
        setChanged(); // Marca el bloque como modificado para guardar la partida
        if (level != null) {
            // Sincroniza los cambios con el cliente para que se renderice al instante
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack removeItem() {
        ItemStack item = this.storedItem;
        this.storedItem = ItemStack.EMPTY;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        return item;
    }

    // --- Lógica del Ritual ---
    public boolean intentarDespertar() {
        if (this.storedItem.isEmpty() || !ModUtils.esArmaValida(this.storedItem)) {
            return false;
        }
        
        // Verificamos si ya está despertada para no duplicar el trabajo
        if (this.storedItem.hasTag() && this.storedItem.getTag().getBoolean("ars_wa.awakened")) {
            return false;
        }

        // Inyectamos el NBT (Igual que en tu vieja receta)
        CompoundTag nbt = this.storedItem.getOrCreateTag();
        nbt.putBoolean("ars_wa.awakened", true);
        this.storedItem.setTag(nbt);
        
        // Sincronizamos con el cliente para que el texto cambie instantáneamente
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    // --- Métodos de guardado y sincronización NBT ---
    
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // Siempre guardamos el estado, incluso si está vacío, para evitar armas fantasma
        tag.put("StoredItem", storedItem.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.storedItem = ItemStack.of(tag.getCompound("StoredItem"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag); // Empaquetamos el ítem obligatoriamente para enviarlo por la red
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (pkt.getTag() != null) {
            this.load(pkt.getTag()); // Obligamos a la pantalla del jugador a leer el ítem nuevo
        }
        // Forzamos al cliente a redibujar visualmente el bloque al instante
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}