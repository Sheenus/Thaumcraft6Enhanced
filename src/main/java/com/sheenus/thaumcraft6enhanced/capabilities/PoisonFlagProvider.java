package com.sheenus.thaumcraft6enhanced.capabilities;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPoisonFlag;
import com.sheenus.thaumcraft6enhanced.util.Reference;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PoisonFlagProvider implements ICapabilitySerializable<NBTTagCompound> {

	@CapabilityInject(value = IPoisonFlag.class)
	public static final Capability<IPoisonFlag> POISON_FLAG = null;
	
	public static final EnumFacing DEFAULT_FACING = null;
	public static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "poison_flag");
	
	private IPoisonFlag instance = POISON_FLAG.getDefaultInstance();
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IPoisonFlag.class, new Capability.IStorage<IPoisonFlag>() {
			@Override
			public NBTBase writeNBT(Capability<IPoisonFlag> capability, IPoisonFlag instance, EnumFacing side) {
				NBTTagCompound nbtPoisonFlag = new NBTTagCompound();
				nbtPoisonFlag.setBoolean("PoisonFlag", instance.getPoisonFlag());
				return nbtPoisonFlag;
			}
			
			@Override
			public void readNBT(Capability<IPoisonFlag> capability, IPoisonFlag instance, EnumFacing side, NBTBase nbt) {
				if (nbt instanceof NBTTagCompound) {
	                instance.setPoisonFlag(((NBTTagCompound) nbt).getBoolean("PoisonFlag"));
				}
			}		
		}, () -> new PoisonFlag());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		
		return capability == POISON_FLAG;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		
		return capability == POISON_FLAG ? POISON_FLAG.<T> cast(this.instance) : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		
		return (NBTTagCompound) POISON_FLAG.getStorage().writeNBT(POISON_FLAG, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		
		 POISON_FLAG.getStorage().readNBT(POISON_FLAG, this.instance, null, nbt);
	}
}
