package com.awakening.ars_wa;


import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.hollingsworth.arsnouveau.api.spell.ISpellCaster;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.api.spell.AbstractCastMethod;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import java.util.ArrayList;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

@Mod.EventBusSubscriber(modid = ArsWA.MODID)
public class ModEvents {

    // Candado de seguridad para prevenir loops infinitos (StackOverflow)
    private static final ThreadLocal<Boolean> IS_CASTING = ThreadLocal.withInitial(() -> false);

    // Obtenemos la capacidad de los hechizos de forma segura gracias a Forge
    public static final Capability<ISpellCaster> SPELL_CASTER_CAP = CapabilityManager.get(new CapabilityToken<ISpellCaster>(){});

    /**
     * Maneja los drops de los mobs para obtener el Fragmento Desgastado.
     */
    @SubscribeEvent
    public static void onMobDrop(LivingDropsEvent event) {
        // Usamos el generador aleatorio de la entidad, es más seguro en Minecraft
        float chance = event.getEntity().getRandom().nextFloat();

        // Lógica para Enderman (Aumentado al 50% temporalmente para que puedas probarlo)
        if (event.getEntity() instanceof EnderMan) {
            if (chance <= 0.50f) {
                addDrop(event, new ItemStack(ModItems.FRAGMENTO_DESGASTADO.get()));
            }
        } 
        // Lógica para el resto de mobs (Aumentado al 10% para probar)
        else {
            if (chance <= 0.10f) {
                addDrop(event, new ItemStack(ModItems.FRAGMENTO_DESGASTADO.get()));
            }
        }
    }

    // Método auxiliar para crear el drop de manera limpia y sin bugs
    private static void addDrop(LivingDropsEvent event, ItemStack dropItem) {
        ItemEntity itemEntity = new ItemEntity(
            event.getEntity().level(), 
            event.getEntity().getX(), 
            event.getEntity().getY(), 
            event.getEntity().getZ(),
            dropItem
        );
        itemEntity.setDefaultPickUpDelay(); // Evita que se buguee al spawnear
        event.getDrops().add(itemEntity);
    }

    /**
     * Le inyecta la capacidad de guardar hechizos a todas las armas válidas
     * para que la Mesa de Inscripción (Scribe's Table) las reconozca.
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        
        if (ModUtils.esArmaValida(stack)) {
            event.addCapability(new ResourceLocation(ArsWA.MODID, "spellcaster"), new ICapabilitySerializable<CompoundTag>() {
                private ISpellCaster caster = null;
                // Enlazamos la creación del SpellCaster a un método para asegurar inicialización segura
                private final LazyOptional<ISpellCaster> optional = LazyOptional.of(this::getCaster);

                private ISpellCaster getCaster() {
                    if (caster == null) {
                        caster = new SpellCaster(stack);
                    }
                    return caster;
                }

                @NotNull
                @Override
                public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    // Siempre ofrecemos la capacidad de hechizos a las armas válidas para que la Scribe's Table las reconozca.
                    // El uso real de esta capacidad (lanzar el hechizo) se controla en el evento onLivingHurt.
                    if (cap == SPELL_CASTER_CAP) {
                        return optional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT() {
                    // Obligamos a la capacidad a que use la serialización interna de Ars Nouveau
                    if (getCaster() instanceof net.minecraftforge.common.util.INBTSerializable) {
                        return (CompoundTag) ((net.minecraftforge.common.util.INBTSerializable<?>) getCaster()).serializeNBT();
                    }
                    return new CompoundTag();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt) {
                    if (getCaster() instanceof net.minecraftforge.common.util.INBTSerializable) {
                        @SuppressWarnings("unchecked")
                        net.minecraftforge.common.util.INBTSerializable<CompoundTag> serializable = 
                                (net.minecraftforge.common.util.INBTSerializable<CompoundTag>) getCaster();
                        serializable.deserializeNBT(nbt);
                    }
                }
            });
        }
    }

    /**
     * Muestra el texto morado cuando un arma ha sido despertada.
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // Verificamos si el ítem tiene el tag NBT que pusimos en el ritual
        if (stack.hasTag() && stack.getTag().contains("ars_wa.awakened")) {
            if (stack.getTag().getBoolean("ars_wa.awakened")) {
                event.getToolTip().add(Component.translatable("tooltip.ars_wa.awakened_weapon"));
            }
        }
    }

    /**
     * Aplica el bono de daño si el jugador usa un arma despertada, basado en su maná.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Si estamos lanzando un hechizo en este momento, salimos para evitar un bucle infinito
        if (IS_CASTING.get()) return;

        // Verificamos que el atacante sea un jugador
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            
            // Verificamos si el arma está despertada
            if (weapon.hasTag() && weapon.getTag().getBoolean("ars_wa.awakened")) {
                
                // ¡Maná activado! Aplica el bono de daño basado en el maná actual del jugador.
                CapabilityRegistry.getMana(player).ifPresent(mana -> {
                    float playerMana = (float) mana.getCurrentMana(); 
                    
                    // Tu regla: cada 20 de maná otorga un 1% (0.01) extra de daño.
                    float bonusPercentage = (playerMana / 20.0f) * 0.01f;
                    
                    float originalDamage = event.getAmount();
                    float extraDamage = originalDamage * bonusPercentage;
                    
                    event.setAmount(originalDamage + extraDamage); // Aplicamos el nuevo daño
                });
                
                // --- LÓGICA DE LA ENCHANTER SWORD (GLIFOS) ---
                weapon.getCapability(SPELL_CASTER_CAP).ifPresent(caster -> {
                    Spell spell = caster.getSpell();
                    // Al no tener una Forma, el sistema base marca spell.isValid() como false.
                    // Por eso, para las espadas, solo verificamos que no esté vacío.
                    if (spell != null && !spell.recipe.isEmpty()) {
                        if (!player.level().isClientSide()) {
                            event.getEntity().invulnerableTime = 0;

                            // Hack maestro: Forzamos el hechizo a ser "Touch" igual que la espada original
                            Spell meleeSpell = new Spell();
                            ArrayList<AbstractSpellPart> newRecipe = new ArrayList<>();
                            newRecipe.add(MethodTouch.INSTANCE); // Siempre a toque
                            
                            // Copiamos solo los efectos que puso el jugador, ignorando la forma que haya elegido
                            for (AbstractSpellPart part : spell.recipe) {
                                if (!(part instanceof AbstractCastMethod)) {
                                    newRecipe.add(part);
                                }
                            }
                            
                            // La Enchanter's Sword original siempre añade Amplify, lo replicamos:
                            newRecipe.add(AugmentAmplify.INSTANCE);
                            meleeSpell.recipe = newRecipe;

                            PlayerCaster wrappedCaster = new PlayerCaster(player);
                            SpellContext context = new SpellContext(
                                player.level(), 
                                caster.modifySpellBeforeCasting(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND, meleeSpell), 
                                player, 
                                wrappedCaster, 
                                weapon
                            );
                            SpellResolver resolver = new SpellResolver(context);
                            EntityHitResult entityRes = new EntityHitResult(event.getEntity());
                            
                            // --- SISTEMA DE DESCUENTO SEGURO (REEMBOLSO) ---
                            double[] manaTracker = new double[2]; // Guarda el maná [0]=antes, [1]=después
                            CapabilityRegistry.getMana(player).ifPresent(m -> manaTracker[0] = m.getCurrentMana());
                            
                            IS_CASTING.set(true); // Ponemos el candado ANTES de lanzar el hechizo
                            try {
                                // Lanzamos el hechizo y dejamos que Ars Nouveau haga sus cálculos de coste
                                resolver.onCastOnEntity(weapon, entityRes.getEntity(), net.minecraft.world.InteractionHand.MAIN_HAND);
                            } finally {
                                IS_CASTING.set(false); // Quitamos el candado SIEMPRE al terminar
                            }
                            
                            // Calculamos cuánto gastó y le devolvemos una parte
                            CapabilityRegistry.getMana(player).ifPresent(m -> {
                                manaTracker[1] = m.getCurrentMana();
                                double costeReal = manaTracker[0] - manaTracker[1];
                                
                                if (costeReal > 0) {
                                    double reembolso = costeReal * 0.25; // 25% de descuento (mitad de precio)
                                    m.addMana(reembolso);
                                }
                            });
                        }
                    }
                });
            }
        }
    }
}
