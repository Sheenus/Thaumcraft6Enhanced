package com.sheenus.thaumcraft6enhanced.capabilities;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPoisonFlag;
import com.sheenus.thaumcraft6enhanced.network.PacketSyncPoisonFlag;
import com.sheenus.thaumcraft6enhanced.network.PacketWrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PoisonFlag implements IPoisonFlag {

	private boolean flag = false;

	@Override
	public boolean getPoisonFlag() {
		return flag;
	}

	@Override
	public void setPoisonFlag(boolean flag) {
		this.flag = flag;	
	}

	@Override
	public NBTTagCompound serializeNBTToTag() {
		final NBTTagCompound poisonFlag = new NBTTagCompound();
        poisonFlag.setBoolean("PoisonFlag", this.flag);
        return poisonFlag;
	}

	@Override
	public void deserializeNBTFromTag(NBTTagCompound tag) {
		if (tag == null) { return; }
		this.flag = tag.getBoolean("PoisonFlag");
	}
	@Override
	public void sync(EntityPlayerMP player) {
		PacketWrapper.INSTANCE.sendTo((IMessage)new PacketSyncPoisonFlag((EntityPlayer)player), player);
		
	}
}
