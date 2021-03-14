package com.sheenus.thaumcraft6enhanced.capabilities;

import com.sheenus.thaumcraft6enhanced.util.Reference;
import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerRunic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PlayerRunicProvider implements ICapabilitySerializable<NBTTagCompound> {
	
	@CapabilityInject(value = IPlayerRunic.class)
	public static final Capability<IPlayerRunic> PLAYER_RUNIC = null;
	
	public static final EnumFacing DEFAULT_FACING = null;
	public static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "runic_shield");
	
	private IPlayerRunic instance = PLAYER_RUNIC.getDefaultInstance();
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IPlayerRunic.class, new Capability.IStorage<IPlayerRunic>() {
			@Override
			public NBTBase writeNBT(Capability<IPlayerRunic> capability, IPlayerRunic instance, EnumFacing side) {
				NBTTagCompound nbtPlayerRunic = new NBTTagCompound();
				nbtPlayerRunic.setFloat("TC.RUNIC", instance.getPlayerRunicHP());
				return nbtPlayerRunic;
			}
			
			@Override
			public void readNBT(Capability<IPlayerRunic> capability, IPlayerRunic instance, EnumFacing side, NBTBase nbt) {
				if (nbt instanceof NBTTagCompound) {
	                instance.setPlayerRunicHP(((NBTTagCompound) nbt).getFloat("TC.RUNIC"));
				}
			}		
		}, () -> new PlayerRunic());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		
		return capability == PLAYER_RUNIC;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		
		return capability == PLAYER_RUNIC ? PLAYER_RUNIC.<T> cast(this.instance) : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		
		return (NBTTagCompound) PLAYER_RUNIC.getStorage().writeNBT(PLAYER_RUNIC, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		
		 PLAYER_RUNIC.getStorage().readNBT(PLAYER_RUNIC, this.instance, null, nbt);
	}
	
}