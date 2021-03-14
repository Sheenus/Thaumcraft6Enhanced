package com.sheenus.thaumcraft6enhanced.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXShield;
import thaumcraft.common.world.aura.AuraHandler;

import java.util.HashMap;

import com.sheenus.thaumcraft6enhanced.Main;
import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerMaxRunic;
import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerRunic;
import com.sheenus.thaumcraft6enhanced.api.capabilities.IPoisonFlag;
import com.sheenus.thaumcraft6enhanced.capabilities.PlayerMaxRunicProvider;
import com.sheenus.thaumcraft6enhanced.capabilities.PlayerRunicProvider;
import com.sheenus.thaumcraft6enhanced.capabilities.PoisonFlagProvider;
import com.sheenus.thaumcraft6enhanced.util.DamageSources;
import com.sheenus.thaumcraft6enhanced.util.Reference;

import baubles.api.BaublesApi;

@EventBusSubscriber(modid = Reference.MOD_ID)
public class PlayerRunicEvents {
	
    private static HashMap<Integer, Long> nextCycle = new HashMap<Integer, Long>();

	@SubscribeEvent
	public static void updateRunic(final LivingUpdateEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {
			handleRunicArmor((EntityPlayer)event.getEntity());
		}
	}
	
	
	@SubscribeEvent
	public static void resetFlag(final PlayerTickEvent event) {
		if (!event.player.getEntityWorld().isRemote && event.phase == Phase.END && event.player.ticksExisted%(event.player.maxHurtResistantTime/2) == 0 && event.player.getCapability(PoisonFlagProvider.POISON_FLAG, null).getPoisonFlag()) {
			event.player.getCapability(PoisonFlagProvider.POISON_FLAG, null).setPoisonFlag(false);
			event.player.getCapability(PoisonFlagProvider.POISON_FLAG, null).sync((EntityPlayerMP)event.player);
			Main.log.info("returned " + event.player.getName() + "'s poison flag to " + event.player.getCapability(PoisonFlagProvider.POISON_FLAG, null).getPoisonFlag());
		}
	}
	
	 /**
     * Handles damage calculations and shield recharge cycle for when a player is hit while under the effects of runic shielding
     * @param event the LivingHurtEvent being watched for by the event handler
     */
	
    @SubscribeEvent(receiveCanceled=true, priority=EventPriority.HIGHEST)
    public static void onRunicArmorDamaged(final LivingHurtEvent event) {
    	EntityLivingBase target = event.getEntityLiving();
    	DamageSource damageSource = event.getSource();
    	float damageAmount = event.getAmount();
    	final long time = System.currentTimeMillis();
    	
    	// checks for the poison effect on the player first, whether the poison effect is ready to fire, and whether the target entity is using runic shielding. If so, changes the DamageSource to a dummy DamageSource type to bypass runic
    	// this is so poison can bypass runic completely.
    	PotionEffect poisonEffect = target.getActivePotionEffect(MobEffects.POISON);
    	if (!target.getEntityWorld().isRemote && hasRunicHP(target) && target.isPotionActive(MobEffects.POISON) && MobEffects.POISON.isReady(poisonEffect.getDuration(), poisonEffect.getAmplifier()) && target.getHealth()>1 && !target.getCapability(PoisonFlagProvider.POISON_FLAG, null).getPoisonFlag()) {
    		if (damageAmount > 1) {
    			damageAmount = 1.0f;
    		}
    		damageSource = DamageSources.POISON;
    		target.getCapability(PoisonFlagProvider.POISON_FLAG, null).setPoisonFlag(true);
    		target.getCapability(PoisonFlagProvider.POISON_FLAG, null).sync((EntityPlayerMP)target);
    		Main.log.info("changed " + target.getName() + "'s poison flag to " + target.getCapability(PoisonFlagProvider.POISON_FLAG, null).getPoisonFlag());
    	}
    	
    	// check for whether a player, with their shield already reduced to 0, should have their PlayerEvents.nextcycle entry reset upon taking further damage.
    	if (hasRunicHP(target) && getRunicHP(target) == 0 && PlayerRunicEvents.canRunicProtectAgainst(damageSource)) {
    		PlayerRunicEvents.nextCycle.put(target.getEntityId(), time + 2*(ModConfig.CONFIG_MISC.shieldWait));
    	}
    	
    	// neat little loop to make sure arrows 'ping' off the shield (i.e. players aren't stuck with arrows through the shield)
    	if ((target instanceof EntityPlayer) && (event.getSource().getDamageType().equals("arrow")) && (getRunicHP(target) > 0)) {
    		target.setArrowCountInEntity((target).getArrowCountInEntity() - 1);
    	}
    	
    	// main damage calculation loop.
    	if ((hasRunicHP(target)) && (getRunicHP(target) > 0) && PlayerRunicEvents.canRunicProtectAgainst(damageSource)) {
    		event.setCanceled(true);
    		
    		// code to get the special damage particles from being hit while using runic shielding
    		int dSource = -1;
	        if (event.getSource().getTrueSource() != null) {
	            dSource = event.getSource().getTrueSource().getEntityId();
	        }
	        PacketHandler.INSTANCE.sendToAllAround((IMessage)new PacketFXShield(event.getEntity().getEntityId(), dSource), new NetworkRegistry.TargetPoint(event.getEntity().world.provider.getDimension(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, 32.0));
    		
    		if (damageAmount <= 0) { return; }
    		damageAmount = applyArmorCalculationsOnRunic(target, damageSource, damageAmount);
    		damageAmount = applyPotionCalculationsOnRunic(target, damageSource, damageAmount);
    		
    		float d = damageAmount;
    		damageAmount = Math.max((damageAmount - getRunicHP(target)), 0.0F);
    		damageAmount = net.minecraftforge.common.ForgeHooks.onLivingDamage(target, damageSource, damageAmount);
    		setRunicHP(target, (getRunicHP(target) - (d - damageAmount)));
    		if (getRunicHP(target) == 0) { 
    			PlayerRunicEvents.nextCycle.put(target.getEntityId(), time + 2*(ModConfig.CONFIG_MISC.shieldWait)); 
    		}
    		else { 
    			PlayerRunicEvents.nextCycle.put(target.getEntityId(), time + ModConfig.CONFIG_MISC.shieldWait); 
    		}
    	}
    }
    	
    /**
     * checks the input DamageSource against the valid types of damage that runic shielding can protect against. 
     * returns true if it can, false if it cannot.
     * @param damageSource the DamageSource in question
     */
    
    public static boolean canRunicProtectAgainst(DamageSource damageSource) { 
    	return  !(damageSource == DamageSources.POISON) &&
    			!(damageSource == DamageSource.WITHER) && 
    			!(damageSource == DamageSource.DROWN) && 
    			!(damageSource == DamageSource.IN_WALL) && 
    			!(damageSource == DamageSource.OUT_OF_WORLD) && 
    			!(damageSource == DamageSource.CRAMMING) && 
    			!(damageSource == DamageSource.STARVE);
    }
    
    /**
     * Calculates the armor calculations to perform against damage done to runic shielding.
     * @param target
     * @param damageSource
     * @param damageAmount
     */
    
    private static float applyArmorCalculationsOnRunic(EntityLivingBase target, DamageSource damageSource, float damageAmount) {
    	if (!damageSource.isUnblockable()) {
            damageAmount = CombatRules.getDamageAfterAbsorb(damageAmount, (float)target.getTotalArmorValue(), 0.0F);
    	}
    	return damageAmount;
    }
    
    /**
     * Calculates the magical resistance available to an entity taking damage while protected by runic shielding.
     * @param target
     * @param damageSource
     * @param damageAmount
     * @return
     */
    
    private static float applyPotionCalculationsOnRunic(EntityLivingBase target, DamageSource damageSource, float damageAmount) {
    	if (damageSource.isDamageAbsolute()) { return damageAmount; }
        else {
            if (target.isPotionActive(MobEffects.RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
                int i = (target.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damageAmount * (float)j;
                damageAmount = f / 25.0F;
            }

            if (damageAmount <= 0.0F) { return 0.0F; }
            else { return damageAmount; }
        }
    }
    
    /**
     * handles control for the runic shielding for the player entity.
     * @param player
     */
    
    private static void handleRunicArmor(final EntityPlayer player) {
    	if (!player.getEntityWorld().isRemote) {		// need to only have the server be calculating everyone's runic shield value, otherwise display glitches occur.
    		// updates the max value for a player's total runic protection, adding or removing them from the relevant runic info hashmaps
    		IPlayerRunic runicCap = player.getCapability(PlayerRunicProvider.PLAYER_RUNIC, null);
    		IPlayerMaxRunic maxRunicCap = player.getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null);
	        int max = 0;
	        // obtains the runic HP values from armor
	        for (int a = 0; a < 4; ++a) {
	            max += getRunicCharge((ItemStack)player.inventory.armorInventory.get(a));
	        }
	        final IInventory baubles = BaublesApi.getBaubles(player);
	        // obtains the runic HP values from baubles
	        for (int a2 = 0; a2 < baubles.getSizeInventory(); ++a2) {
	            max += getRunicCharge(baubles.getStackInSlot(a2));
	        }
	        // records the player's maximum runic HP should they have any.
	        if (max > 0) {
	        	setMaxRunic(player, max);
	            
	            if (!hasRunicHP(player)) {
	            	setRunicHP(player, 0.0F);
	            }
	            if (getRunicHP(player) > max) {
	            	setRunicHP(player, max);
	            }
	        }
	        else {// removes the player from the runic max hashmap and resets their runic shield capability to 0 should they not have any
	        	setMaxRunic(player, 0);
	            setRunicHP(player, 0.0F);
	        }
	    	// Check to see if the player has any runic shielding (via checking the value of the player's max runic value), if they do but aren't in the recharge hashmap (lastCycle), put em in it
	        if (hasRunicHP(player)) {
	            if (!PlayerRunicEvents.nextCycle.containsKey(player.getEntityId())) {
	                PlayerRunicEvents.nextCycle.put(player.getEntityId(), 0L);
	            }
	            final long time = System.currentTimeMillis();
	            final float charge = getRunicHP(player);	
	            
	            if (charge < getMaxRunic(player) && PlayerRunicEvents.nextCycle.get(player.getEntityId()) < time && !AuraHandler.shouldPreserveAura(player.world, player, player.getPosition()) && AuraHelper.getVis(player.world, new BlockPos((Entity)player)) >= ModConfig.CONFIG_MISC.shieldCost) {
	            	AuraHandler.drainVis(player.world, new BlockPos((Entity)player), (float)ModConfig.CONFIG_MISC.shieldCost, false);
	                PlayerRunicEvents.nextCycle.put(player.getEntityId(), time + (ModConfig.CONFIG_MISC.shieldRecharge));
	                setRunicHP(player, (charge + 1));
	                if (getRunicHP(player) > getMaxRunic(player)) {	// to stop runic shielding overflow and ugly graphical 'errors'
	                	setRunicHP(player, getMaxRunic(player));
	                }
	            }
	        }
    	}
    }
    
    public static boolean hasRunicHP(final EntityLivingBase player) {
    	return (player instanceof EntityPlayer) && getMaxRunic(player) > 0;
    }
    
    public static float getRunicHP(final EntityLivingBase player) {
    	IPlayerRunic runicCap = player.getCapability(PlayerRunicProvider.PLAYER_RUNIC, null);
    	return runicCap.getPlayerRunicHP();
    }
    
    public static int getMaxRunic(final EntityLivingBase player) {
    	IPlayerMaxRunic maxRunicCap = player.getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null);
    	return maxRunicCap.getPlayerMaxRunic();
    }
    
    private static void setMaxRunic(final EntityLivingBase player, int amount) {
    	IPlayerMaxRunic maxRunicCap = player.getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null);
    	maxRunicCap.setPlayerMaxRunic(amount);
    	if (!player.getEntityWorld().isRemote && player instanceof EntityPlayerMP) {
    		maxRunicCap.sync((EntityPlayerMP)player);
    	}
    }
    
    private static void setRunicHP(final EntityLivingBase player, float amount) {
    	IPlayerRunic runicCap = player.getCapability(PlayerRunicProvider.PLAYER_RUNIC, null);
    	runicCap.setPlayerRunicHP(amount);
    	if (!player.getEntityWorld().isRemote && player instanceof EntityPlayerMP) {
    		runicCap.sync((EntityPlayerMP)player);
    	}
    }
    
    public static int getRunicCharge(final ItemStack stack) {
        int base = 0;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("TC.RUNIC")) {
            base += stack.getTagCompound().getByte("TC.RUNIC");
        }
        return base;
    }
	
    @SubscribeEvent
    public static void attachRunicCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PlayerRunicProvider.NAME, new PlayerRunicProvider());
            event.addCapability(PoisonFlagProvider.NAME, new PoisonFlagProvider());
            event.addCapability(PlayerMaxRunicProvider.NAME, new PlayerMaxRunicProvider());
        }
    }
    
    @SubscribeEvent
    public static void syncCapabilitiesOnPlayerJoin(final EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
            final IPlayerRunic pr = player.getCapability(PlayerRunicProvider.PLAYER_RUNIC, null);
            final IPoisonFlag pf = player.getCapability(PoisonFlagProvider.POISON_FLAG, null);
            final IPlayerMaxRunic pmr = player.getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null);
            if (pr != null) {
                pr.sync(player);
            }
            if (pf != null) {
                pf.sync(player);
            }
            if (pmr != null) {
                pmr.sync(player);
            }
        }
    }
    
    @SubscribeEvent
    public static void cloneRunicCapabilities(final PlayerEvent.Clone event) {
        try {
            if (!(event.isWasDeath())) {
            	final NBTTagCompound nbtRunic = (event.getOriginal()).getCapability(PlayerRunicProvider.PLAYER_RUNIC, null).serializeNBTToTag();
            	final NBTTagCompound nbtFlag = (event.getOriginal()).getCapability(PoisonFlagProvider.POISON_FLAG, null).serializeNBTToTag();
            	final NBTTagCompound nbtMaxRunic = (event.getOriginal()).getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null).serializeNBTToTag();
            	(event.getEntityPlayer()).getCapability(PlayerRunicProvider.PLAYER_RUNIC, null).deserializeNBTFromTag(nbtRunic);
            	(event.getEntityPlayer()).getCapability(PoisonFlagProvider.POISON_FLAG, null).deserializeNBTFromTag(nbtFlag);
            	(event.getEntityPlayer()).getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null).deserializeNBTFromTag(nbtMaxRunic);
            }
        }
        catch (Exception e) {
            Main.log.error("Could not clone player [" + event.getOriginal().getName() + "]'s runic shielding capabilities when changing dimensions");
        }
    }
}
