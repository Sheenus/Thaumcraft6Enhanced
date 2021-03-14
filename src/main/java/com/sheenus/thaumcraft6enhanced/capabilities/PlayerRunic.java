package com.sheenus.thaumcraft6enhanced.capabilities;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerRunic;
import com.sheenus.thaumcraft6enhanced.network.PacketWrapper;
import com.sheenus.thaumcraft6enhanced.network.PacketSyncRunic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PlayerRunic implements IPlayerRunic {

	private float runicShield;
	
	@Override
	public float getPlayerRunicHP() {
		return runicShield;
	}

	@Override
	public void setPlayerRunicHP(float shieldStrength) {
		this.runicShield = shieldStrength;
	}

	@Override
	public NBTTagCompound serializeNBTToTag() {
		final NBTTagCompound playerRunicTag = new NBTTagCompound();
        playerRunicTag.setFloat("TC.RUNIC", this.runicShield);
        return playerRunicTag;
	}

	@Override
	public void deserializeNBTFromTag(NBTTagCompound tag) {
		if (tag == null) { return; }
		this.runicShield = tag.getFloat("TC.RUNIC");
	}
	@Override
	public void sync(EntityPlayerMP player) {
		PacketWrapper.INSTANCE.sendTo((IMessage)new PacketSyncRunic((EntityPlayer)player), player);
		
	}
}
