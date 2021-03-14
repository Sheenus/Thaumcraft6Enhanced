package com.sheenus.thaumcraft6enhanced.network;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerMaxRunic;
import com.sheenus.thaumcraft6enhanced.capabilities.PlayerMaxRunicProvider;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.common.lib.utils.Utils;

public class PacketSyncMaxRunic implements IMessage, IMessageHandler<PacketSyncMaxRunic, IMessage> {
	
	protected NBTTagCompound data;
	
	public PacketSyncMaxRunic() {
    }
	
	public PacketSyncMaxRunic(final EntityPlayer player) {
		final IPlayerMaxRunic pmr = player.getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null);
		this.data = pmr.serializeNBTToTag();
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.data = Utils.readNBTTagCompoundFromBuffer(buffer);	
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		Utils.writeNBTTagCompoundToBuffer(buffer, this.data);
	}

	@Override
	public IMessage onMessage(PacketSyncMaxRunic message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				final EntityPlayer player = Minecraft.getMinecraft().player;
				final IPlayerMaxRunic pmr = player.getCapability(PlayerMaxRunicProvider.PLAYER_MAX_RUNIC, null);
				pmr.deserializeNBTFromTag(message.data);
			}
		});
		return null;
	}
	
}
