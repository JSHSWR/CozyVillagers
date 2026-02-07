package com.koen.cozyvillagers.favors;

import com.koen.cozyvillagers.data.FamiliarityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;

import java.util.*;

public class FavorManager {
    private static final Map<UUID, Favor> ACTIVE = new HashMap<>();
    private static final Random R = new Random();

    public static String getFavorStatus(EntityPlayer player) {
        Favor f = ACTIVE.get(player.getUniqueID());
        return f == null ? null : f.getDescription();
    }

    public static void assignFavor(EntityPlayer player, EntityVillager villager) {
        if (!com.koen.cozyvillagers.config.CozyConfig.enableFavors) return;

        UUID pid = player.getUniqueID();
        if (ACTIVE.containsKey(pid)) {
            player.addChatMessage(new ChatComponentText("You already have an active favor."));
            return;
        }

        Favor favor;
        int choice = R.nextInt(3);
        switch (choice) {
            case 0:
                favor = new FavorBringItems("Bring: 10 wheat OR 6 bread OR 8 carrots",
                        new ItemRequirement(Items.wheat, 10),
                        new ItemRequirement(Items.bread, 6),
                        new ItemRequirement(Items.carrot, 8));
                break;
            case 1:
                favor = new FavorBringItems("Bring: 5 flowers (red OR yellow)",
                        new ItemRequirement(Item.getItemFromBlock(Blocks.red_flower), 5),
                        new ItemRequirement(Item.getItemFromBlock(Blocks.yellow_flower), 5));
                break;
            default:
                favor = new FavorBringItems("Bring: 16 seeds",
                        new ItemRequirement(Items.wheat_seeds, 16));
                break;
        }

        ACTIVE.put(pid, favor);
        player.addChatMessage(new ChatComponentText("New favor: " + favor.getDescription()));
    }

    public static void checkCompletion(EntityPlayer player, EntityVillager villager) {
        UUID pid = player.getUniqueID();
        Favor favor = ACTIVE.get(pid);
        if (favor == null) return;

        if (!favor.isComplete(player)) return;

        favor.complete(player);
        ACTIVE.remove(pid);

        player.inventory.addItemStackToInventory(new ItemStack(Items.bread, 1));
        if (R.nextFloat() < 0.5f) {
            player.inventory.addItemStackToInventory(new ItemStack(Items.emerald, R.nextInt(2) + 1));
        }

        FamiliarityData.increaseFamiliarity(player, villager, 5);

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            mp.playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S2APacketParticles(
                    EnumParticleTypes.VILLAGER_HAPPY, false,
                    (float) villager.posX, (float) villager.posY + 1.0F, (float) villager.posZ,
                    0F, 0F, 0F, 1F, 6, new int[0]
            ));
        }

        player.addChatMessage(new ChatComponentText("Favor complete! Thanks — here’s a small reward."));
    }

    public static abstract class Favor {
        private final String description;
        protected Favor(String description) { this.description = description; }
        public String getDescription() { return description; }
        public abstract boolean isComplete(EntityPlayer player);
        public abstract void complete(EntityPlayer player);
    }

    public static class FavorBringItems extends Favor {
        private final List<ItemRequirement> reqs;
        public FavorBringItems(String description, ItemRequirement... reqs) {
            super(description);
            this.reqs = Arrays.asList(reqs);
        }
        @Override
        public boolean isComplete(EntityPlayer player) {
            for (ItemRequirement r : reqs) if (r.matches(player)) return true;
            return false;
        }
        @Override
        public void complete(EntityPlayer player) {
            for (ItemRequirement r : reqs) {
                if (r.matches(player)) { r.consume(player); return; }
            }
        }
    }

    public static class ItemRequirement {
        public final Item item;
        public final int amount;
        public ItemRequirement(Item item, int amount) {
            this.item = item; this.amount = amount;
        }
        public boolean matches(EntityPlayer player) {
            int count = 0;
            for (ItemStack s : player.inventory.mainInventory) {
                if (s != null && s.getItem() == item) {
                    count += s.stackSize;
                    if (count >= amount) return true;
                }
            }
            return false;
        }
        public void consume(EntityPlayer player) {
            int remaining = amount;
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack s = player.inventory.mainInventory[i];
                if (s != null && s.getItem() == item) {
                    int take = Math.min(s.stackSize, remaining);
                    s.stackSize -= take;
                    if (s.stackSize <= 0) player.inventory.mainInventory[i] = null;
                    remaining -= take;
                    if (remaining <= 0) return;
                }
            }
        }
    }
}
