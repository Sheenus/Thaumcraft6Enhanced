package com.sheenus.thaumcraft6enhanced.capabilities;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerMaxRunic;
import com.sheenus.thaumcraft6enhanced.network.PacketSyncMaxRunic;
import com.sheenus.thaumcraft6enhanced.network.PacketSyncRunic;
import com.sheenus.thaumcraft6enhanced.network.PacketWrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PlayerMaxRunic implements IPlayerMaxRunic {
	
private int maxRunicShield = 0;
	
	@Override
	public int getPlayerMaxRunic() {
		return maxRunicShield;
	}

	@Override
	public void setPlayerMaxRunic(int shieldStrength) {
		this.maxRunicShield = shieldStrength;
	}

	@Override
	public NBTTagCompound serializeNBTToTag() {
		final NBTTagCompound playerMaxRunicTag = new NBTTagCompound();
        playerMaxRunicTag.setInteger("TC.MAXRUNIC", this.maxRunicShield);
        return playerMaxRunicTag;
	}

	@Override
	public void deserializeNBTFromTag(NBTTagCompound tag) {
		if (tag == null) { return; }
		this.maxRunicShield = tag.getInteger("TC.MAXRUNIC");
	}
	@Override
	public void sync(EntityPlayerMP player) {
		PacketWrapper.INSTANCE.sendTo((IMessage)new PacketSyncMaxRunic((EntityPlayer)player), player);
		
	}
	
}
