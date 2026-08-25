package com.awakening.ars_wa;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;
import com.hollingsworth.arsnouveau.api.spell.ISpellCaster;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class AparatoDespertarBlock extends BaseEntityBlock {

    // Propiedad para definir hacia dónde mira el bloque
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Cajas de colisión exactas rotadas para cada dirección
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 15.0D, 14.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 2.0D, 16.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(1.0D, 0.0D, 0.0D, 14.0D, 15.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.box(2.0D, 0.0D, 0.0D, 15.0D, 15.0D, 16.0D);

    public AparatoDespertarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Cuando el jugador lo coloca, el frente apuntará hacia el jugador
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AparatoDespertarBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) { // Sólo el servidor maneja los ítems reales
            BlockEntity blockEntity = level.getBlockEntity(pos);
            
            if (blockEntity instanceof AparatoDespertarBlockEntity aparatoBE) {
                ItemStack handItem = player.getItemInHand(hand);
                ItemStack storedItem = aparatoBE.getStoredItem();

                // 1. Si el jugador hace click con la Empuñadura Arcana y hay un arma válida
                if (handItem.getItem() == ModItems.EMPUNADURA_ARCANA.get() && !storedItem.isEmpty()) {
                    boolean exito = aparatoBE.intentarDespertar();
                    if (exito) {
                        // Consumimos 1 empuñadura como tributo del ritual
                        if (!player.isCreative()) {
                            handItem.shrink(1); 
                        }
                        
                        // --- Efectos de sonido y partículas del Despertar ---
                        level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.2F);
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, 
                                pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 
                                25, // Cantidad de partículas
                                0.2, 0.2, 0.2, // Dispersión (X, Y, Z)
                                0.1); // Velocidad
                        }
                    }
                    return InteractionResult.SUCCESS;
                }

                // 2. Si el aparato tiene un arma despertada y usamos un Libro de Hechizos
                if (!storedItem.isEmpty() && storedItem.hasTag() && storedItem.getTag().getBoolean("ars_wa.awakened") && !handItem.isEmpty()) {
                    Spell spell = null;

                    // Intentamos leer el hechizo del ítem (Libro o Pergamino)
                    LazyOptional<ISpellCaster> cap = handItem.getCapability(ModEvents.SPELL_CASTER_CAP);
                    if (cap.isPresent()) {
                        spell = cap.resolve().get().getSpell();
                    } else if (handItem.hasTag()) {
                        // Si es un pergamino, forzamos la lectura de su memoria NBT en crudo
                        ISpellCaster tempCaster = new SpellCaster(handItem);
                        spell = tempCaster.getSpell();
                    }

                    if (spell != null && spell.recipe != null && !spell.recipe.isEmpty()) {
                        final Spell finalSpell = spell;
                        storedItem.getCapability(ModEvents.SPELL_CASTER_CAP).ifPresent(swordCaster -> {
                            swordCaster.setSpell(finalSpell);
                            aparatoBE.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);
                            
                            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5¡Hechizo grabado en el arma!"), true);
                            
                            // ¡Inmersión! Consumimos el pergamino como parte del ritual
                            if (!player.isCreative() && net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(handItem.getItem()).getPath().contains("parchment")) {
                                handItem.shrink(1);
                            }
                        });
                        return InteractionResult.SUCCESS;
                    } else if (!handItem.isEmpty() && net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(handItem.getItem()).getPath().contains("parchment")) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cEl pergamino está vacío. Escribe un hechizo en él primero."), true);
                        return InteractionResult.SUCCESS;
                    }
                }

                // 3. Si el aparato está vacío, intentamos colocar un arma
                if (storedItem.isEmpty()) {
                    if (!handItem.isEmpty() && ModUtils.esArmaValida(handItem)) {
                        ItemStack toInsert = handItem.copy();
                        toInsert.setCount(1); // Solo insertamos 1
                        aparatoBE.setStoredItem(toInsert);
                        handItem.shrink(1);   // Se lo quitamos al jugador de la mano
                        return InteractionResult.SUCCESS;
                    }
                } 
                // 4. Si el aparato ya tiene un arma y usamos la mano vacía, se la devolvemos
                else if (handItem.isEmpty()) {
                    ItemStack itemToGive = aparatoBE.removeItem();
                    // Usamos el método oficial de Forge para evitar bugs de desincronización de slots
                    net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(player, itemToGive);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}