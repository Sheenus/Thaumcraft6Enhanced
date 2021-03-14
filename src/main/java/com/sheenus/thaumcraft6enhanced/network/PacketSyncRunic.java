package com.sheenus.thaumcraft6enhanced.network;

import com.sheenus.thaumcraft6enhanced.api.capabilities.IPlayerRunic;
import com.sheenus.thaumcraft6enhanced.capabilities.PlayerRunicProvider;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.common.lib.utils.Utils;

public class PacketSyncRunic implements IMessage, IMessageHandler<PacketSyncRunic, IMessage> {
	
	protected NBTTagCompound data;
	
	public PacketSyncRunic() {
    }
	
	public PacketSyncRunic(final EntityPlayer player) {
		final IPlayerRunic pr = player.getCapability(PlayerRunicProvider.PLAYER_RUNIC, null);
		this.data = pr.serializeNBTToTag();
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
	public IMessage onMessage(PacketSyncRunic message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				final EntityPlayer player = Minecraft.getMinecraft().player;
				final IPlayerRunic pr = player.getCapability(PlayerRunicProvider.PLAYER_RUNIC, null);
				pr.deserializeNBTFromTag(message.data);
			}
		});
		return null;
	}
}
