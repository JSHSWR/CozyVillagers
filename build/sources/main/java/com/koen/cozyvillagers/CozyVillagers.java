package com.koen.cozyvillagers;

import com.koen.cozyvillagers.commands.CommandCozy;
import com.koen.cozyvillagers.config.CozyConfig;
import com.koen.cozyvillagers.network.PacketHandler;
import com.koen.cozyvillagers.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CozyVillagers.MODID, name = CozyVillagers.NAME, version = CozyVillagers.VERSION,
        acceptedMinecraftVersions = "[1.8.9]", useMetadata = true)
public class CozyVillagers {
    public static final String MODID = "cozyvillagers";
    public static final String NAME = "Cozy Villagers";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static CozyVillagers instance;

    @SidedProxy(
            clientSide = "com.koen.cozyvillagers.proxy.ClientProxy",
            serverSide = "com.koen.cozyvillagers.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CozyConfig.init(event.getSuggestedConfigurationFile());
        PacketHandler.init();

        proxy.preInit(event);

        // Register our event handler only once on the main Forge event bus.
        // Registering the same handler on both the Forge bus and the FML bus causes
        // certain events (like EntityInteractEvent) to fire twice, leading to
        // duplicated dialogue openings and favor completions.
        // See: net.minecraftforge.common.MinecraftForge.EVENT_BUS vs
        // net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().
        com.koen.cozyvillagers.events.EventHandler handler = new com.koen.cozyvillagers.events.EventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCozy());
    }
}
