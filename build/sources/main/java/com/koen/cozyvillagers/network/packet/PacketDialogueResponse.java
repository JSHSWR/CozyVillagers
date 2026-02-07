package com.koen.cozyvillagers.network.packet;

import com.koen.cozyvillagers.dialogue.DialogueManager;
import com.koen.cozyvillagers.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDialogueResponse implements IMessage {
    private String villagerId;
    private int selectedIndex;

    public PacketDialogueResponse() {}

    public PacketDialogueResponse(String villagerId, int selectedIndex) {
        this.villagerId = villagerId;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        villagerId = ByteBufUtils.readUTF8String(buf);
        selectedIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, villagerId);
        buf.writeInt(selectedIndex);
    }

    public static class Handler implements IMessageHandler<PacketDialogueResponse, IMessage> {
        @Override
        public IMessage onMessage(PacketDialogueResponse message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            Entity target = null;
            for (Object obj : world.loadedEntityList) {
                if (obj instanceof net.minecraft.entity.passive.EntityVillager) {
                    net.minecraft.entity.passive.EntityVillager v = (net.minecraft.entity.passive.EntityVillager) obj;
                    if (v.getUniqueID().toString().equals(message.villagerId)) {
                        target = v;
                        break;
                    }
                }
            }

            if (target instanceof net.minecraft.entity.passive.EntityVillager) {
                net.minecraft.entity.passive.EntityVillager villager = (net.minecraft.entity.passive.EntityVillager) target;

                DialogueManager.DialogueReply reply =
                        DialogueManager.handleResponse(player, villager, message.selectedIndex);

                if (reply != null) {
                    PacketHandler.INSTANCE.sendTo(
                            new PacketUpdateDialogue(
                                    message.villagerId,
                                    reply.villagerName,
                                    reply.profession,
                                    reply.tier,
                                    reply.text,
                                    reply.responses,
                                    reply.familiarity
                            ),
                            player
                    );
                }
            }

            return null;
        }
    }
}
