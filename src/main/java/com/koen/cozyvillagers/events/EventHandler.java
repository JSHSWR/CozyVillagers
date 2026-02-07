package com.koen.cozyvillagers.events;

import com.koen.cozyvillagers.config.CozyConfig;
import com.koen.cozyvillagers.dialogue.DialogueManager;
import com.koen.cozyvillagers.favors.FavorManager;
import com.koen.cozyvillagers.network.PacketHandler;
import com.koen.cozyvillagers.network.packet.PacketOpenDialogue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EventHandler {
    private static final List<String> NAMES = Arrays.asList(
            "Alice","Bob","Charlie","Daisy","Elliot","Fiona","Gareth","Hazel","Ivy","Jacob","Kira","Liam"
    );

    /**
     * Predefined chat bubble phrases.
     * These strings were previously allocated on every world tick; moving them
     * into a static final array avoids the per‑tick object creation and reduces
     * GC pressure.
     */
    private static final String[] BUBBLE_PHRASES = {
            "Hmm…",
            "What a lovely day",
            "I need more wheat",
            "Greetings",
            "Stay awhile",
            "Such fine weather"
    };

    private final Random r = new Random();

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.entityPlayer.worldObj.isRemote) return;

        EntityPlayer player = event.entityPlayer;
        Entity target = event.target;

        if (!(target instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) target;

        if (player.isSneaking()) return;
        if (!CozyConfig.enableDialogue) return;

        event.setCanceled(true);

        if (CozyConfig.enableFavors) {
            FavorManager.checkCompletion(player, villager);
        }

        DialogueManager.DialogueReply reply = DialogueManager.openConversation(player, villager);

        PacketHandler.INSTANCE.sendTo(new PacketOpenDialogue(
                villager.getUniqueID().toString(),
                reply.villagerName,
                reply.profession,
                reply.tier,
                reply.text,
                reply.responses,
                reply.familiarity
        ), (EntityPlayerMP) player);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityVillager)) return;
        EntityVillager v = (EntityVillager) event.entity;
        if (v.worldObj.isRemote) return;

        if (!v.hasCustomName()) {
            v.setCustomNameTag(NAMES.get(r.nextInt(NAMES.size())));
            v.setAlwaysRenderNameTag(true);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        long worldTime = event.world.getTotalWorldTime();

        if (CozyConfig.enableBubbles && worldTime % 20 == 0) {
            double chancePerSecond = CozyConfig.bubbleChancePerMinute / 60.0;

            for (Object obj : event.world.loadedEntityList) {
                if (!(obj instanceof EntityVillager)) continue;
                EntityVillager v = (EntityVillager) obj;

                NBTTagCompound data = v.getEntityData();
                if (data.hasKey("CozyBubbleUntil") && worldTime <= data.getLong("CozyBubbleUntil")) continue;

                if (r.nextDouble() < chancePerSecond) {
                    // Pick a phrase from the pre‑defined array.  Using the constant avoids allocating a new array each tick.
                    data.setString("CozyBubbleText", BUBBLE_PHRASES[r.nextInt(BUBBLE_PHRASES.length)]);
                    data.setLong("CozyBubbleUntil", worldTime + 60);
                }
            }
        }

        int intervalTicks = CozyConfig.aiNudgeIntervalSeconds * 20;
        if (intervalTicks > 0 && worldTime % intervalTicks == 0) {
            long tod = worldTime % 24000L;
            if (tod >= 12000 && tod <= 14000) {
                for (Object obj : event.world.loadedEntityList) {
                    if (!(obj instanceof EntityVillager)) continue;
                    EntityVillager v = (EntityVillager) obj;

                    double tx = v.posX + (r.nextDouble() - 0.5) * 8;
                    double tz = v.posZ + (r.nextDouble() - 0.5) * 8;
                    v.getNavigator().tryMoveToXYZ(tx, v.posY, tz, 0.3);
                }
            }
        }
    }
}
