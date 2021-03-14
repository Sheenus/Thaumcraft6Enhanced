package com.sheenus.thaumcraft6enhanced.network;

import com.sheenus.thaumcraft6enhanced.util.Reference;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketWrapper {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
	
	public static void registerPacketHandlers() {
	int idx = 0;
	PacketWrapper.INSTANCE.registerMessage(PacketSyncRunic.class, PacketSyncRunic.class, idx++, Side.CLIENT);
	PacketWrapper.INSTANCE.registerMessage(PacketSyncPoisonFlag.class, PacketSyncPoisonFlag.class, idx++, Side.CLIENT);
	PacketWrapper.INSTANCE.registerMessage(PacketSyncMaxRunic.class, PacketSyncMaxRunic.class, idx++, Side.CLIENT);
	}
}
