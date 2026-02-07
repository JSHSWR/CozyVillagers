package com.koen.cozyvillagers.proxy;

import com.koen.cozyvillagers.client.render.ChatBubbleRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new ChatBubbleRenderer());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}