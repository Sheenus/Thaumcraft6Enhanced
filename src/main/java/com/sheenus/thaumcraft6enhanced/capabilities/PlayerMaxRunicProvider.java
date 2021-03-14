package com.sheenus.thaumcraft6enhanced.capabilities;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerMaxRunic;
import com.sheenus.thaumcraft6enhanced.util.Reference;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PlayerMaxRunicProvider implements ICapabilitySerializable<NBTTagCompound> {
	
	@CapabilityInject(value = IPlayerMaxRunic.class)
	public static final Capability<IPlayerMaxRunic> PLAYER_MAX_RUNIC = null;
	
	public static final EnumFacing DEFAULT_FACING = null;
	public static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "runic_shield_max");
	
	private IPlayerMaxRunic instance = PLAYER_MAX_RUNIC.getDefaultInstance();
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IPlayerMaxRunic.class, new Capability.IStorage<IPlayerMaxRunic>() {
			@Override
			public NBTBase writeNBT(Capability<IPlayerMaxRunic> capability, IPlayerMaxRunic instance, EnumFacing side) {
				NBTTagCompound nbtPlayerMaxRunic = new NBTTagCompound();
				nbtPlayerMaxRunic.setInteger("TC.MAXRUNIC", instance.getPlayerMaxRunic());
				return nbtPlayerMaxRunic;
			}
			
			@Override
			public void readNBT(Capability<IPlayerMaxRunic> capability, IPlayerMaxRunic instance, EnumFacing side, NBTBase nbt) {
				if (nbt instanceof NBTTagCompound) {
	                instance.setPlayerMaxRunic(((NBTTagCompound) nbt).getInteger("TC.MAXRUNIC"));
				}
			}		
		}, () -> new PlayerMaxRunic());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		
		return capability == PLAYER_MAX_RUNIC;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		
		return capability == PLAYER_MAX_RUNIC ? PLAYER_MAX_RUNIC.<T> cast(this.instance) : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		
		return (NBTTagCompound) PLAYER_MAX_RUNIC.getStorage().writeNBT(PLAYER_MAX_RUNIC, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		
		 PLAYER_MAX_RUNIC.getStorage().readNBT(PLAYER_MAX_RUNIC, this.instance, null, nbt);
	}
	
}
