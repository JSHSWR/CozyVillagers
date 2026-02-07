package com.koen.cozyvillagers.network.packet;

import com.koen.cozyvillagers.client.gui.GuiDialogue;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketOpenDialogue implements IMessage {
    private String villagerId;
    private String villagerName;
    private String profession;
    private int tier;
    private String text;
    private List<String> responses;
    private int familiarity;

    public PacketOpenDialogue() {}

    public PacketOpenDialogue(String villagerId, String villagerName, String profession,
                              int tier, String text, List<String> responses, int familiarity) {
        this.villagerId = villagerId;
        this.villagerName = villagerName;
        this.profession = profession;
        this.tier = tier;
        this.text = text;
        this.responses = responses;
        this.familiarity = familiarity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        villagerId = ByteBufUtils.readUTF8String(buf);
        villagerName = ByteBufUtils.readUTF8String(buf);
        profession = ByteBufUtils.readUTF8String(buf);
        tier = buf.readInt();
        text = ByteBufUtils.readUTF8String(buf);

        int size = buf.readInt();
        responses = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            responses.add(ByteBufUtils.readUTF8String(buf));
        }

        familiarity = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, villagerId);
        ByteBufUtils.writeUTF8String(buf, villagerName);
        ByteBufUtils.writeUTF8String(buf, profession);
        buf.writeInt(tier);
        ByteBufUtils.writeUTF8String(buf, text);

        buf.writeInt(responses.size());
        for (String s : responses) {
            ByteBufUtils.writeUTF8String(buf, s);
        }

        buf.writeInt(familiarity);
    }

    public static class Handler implements IMessageHandler<PacketOpenDialogue, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenDialogue message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().displayGuiScreen(
                        new GuiDialogue(
                                message.villagerId,
                                message.villagerName,
                                message.profession,
                                message.tier,
                                message.text,
                                message.responses,
                                message.familiarity
                        )
                );
            });
            return null;
        }
    }
}
