package com.sheenus.thaumcraft6enhanced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sheenus.thaumcraft6enhanced.capabilities.PlayerMaxRunicProvider;
import com.sheenus.thaumcraft6enhanced.capabilities.PlayerRunicProvider;
import com.sheenus.thaumcraft6enhanced.capabilities.PoisonFlagProvider;
import com.sheenus.thaumcraft6enhanced.network.PacketWrapper;
import com.sheenus.thaumcraft6enhanced.proxy.IProxy;
import com.sheenus.thaumcraft6enhanced.util.Reference;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, 
	 name = Reference.NAME, 
	 version = Reference.VERSION, 
	 acceptedMinecraftVersions = Reference.ACCEPTED_VERSIONS, 
	 dependencies = Reference.DEPENDANCY)
public class Main {
	
	@Instance(Reference.MOD_ID)
	public static Main instance;
	public static final Logger log = LogManager.getLogger("thaumcraft6enhanced");
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static IProxy proxy;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		
		PlayerRunicProvider.register();
		PoisonFlagProvider.register();
		PlayerMaxRunicProvider.register();
		PacketWrapper.registerPacketHandlers();
		
		proxy.preInit(event);
		
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event) {
		
		proxy.init(event);
	}
	
	@EventHandler
	public static void postInit(FMLPostInitializationEvent event) {
		
		proxy.postInit(event);
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		
		proxy.serverStarting(event);
	}
}
