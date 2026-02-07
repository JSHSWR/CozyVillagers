package com.koen.cozyvillagers.network;

import com.koen.cozyvillagers.CozyVillagers;
import com.koen.cozyvillagers.network.packet.PacketDialogueResponse;
import com.koen.cozyvillagers.network.packet.PacketOpenDialogue;
import com.koen.cozyvillagers.network.packet.PacketUpdateDialogue;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    private static final String CHANNEL = CozyVillagers.MODID;
    public static SimpleNetworkWrapper INSTANCE;
    private static int nextId;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
        nextId = 0;

        // Server -> Client: open GUI (includes familiarity)
        INSTANCE.registerMessage(PacketOpenDialogue.Handler.class, PacketOpenDialogue.class, nextId++, Side.CLIENT);

        // Client -> Server: player picked a response
        INSTANCE.registerMessage(PacketDialogueResponse.Handler.class, PacketDialogueResponse.class, nextId++, Side.SERVER);

        // Server -> Client: update the *existing* GUI in-place (text/buttons/tier/familiarity)
        INSTANCE.registerMessage(PacketUpdateDialogue.Handler.class, PacketUpdateDialogue.class, nextId++, Side.CLIENT);
    }
}
