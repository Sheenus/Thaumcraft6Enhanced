package com.sheenus.thaumcraft6enhanced.api.capabilities;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerMaxRunic {

	int getPlayerMaxRunic();
	
	void setPlayerMaxRunic(int maxRunicShieldStrength);

	NBTTagCompound serializeNBTToTag();
	
	void deserializeNBTFromTag(NBTTagCompound tag);
	
	void sync(EntityPlayerMP player);
}
