package com.koen.cozyvillagers.data;

import com.koen.cozyvillagers.config.CozyConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FamiliarityData extends WorldSavedData {

    private static final String IDENTIFIER = "cozy_familiarity";

    // playerUUID -> (villagerUUID -> record)
    private final Map<UUID, Map<UUID, Record>> map = new HashMap<>();

    public FamiliarityData() {
        super(IDENTIFIER);
    }

    public FamiliarityData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        map.clear();

        NBTTagList players = nbt.getTagList("players", 10); // 10 = compound
        for (int i = 0; i < players.tagCount(); i++) {
            NBTTagCompound pTag = players.getCompoundTagAt(i);
            if (!pTag.hasKey("player")) continue;

            UUID pId;
            try {
                pId = UUID.fromString(pTag.getString("player"));
            } catch (Exception ignored) {
                continue;
            }

            Map<UUID, Record> inner = new HashMap<>();
            NBTTagList villagers = pTag.getTagList("villagers", 10);

            for (int j = 0; j < villagers.tagCount(); j++) {
                NBTTagCompound vTag = villagers.getCompoundTagAt(j);
                if (!vTag.hasKey("villager")) continue;

                UUID vId;
                try {
                    vId = UUID.fromString(vTag.getString("villager"));
                } catch (Exception ignored) {
                    continue;
                }

                Record r = new Record();
                r.familiarity = vTag.getInteger("fam");
                r.lastDay = vTag.getLong("day");
                r.dailyGain = vTag.getInteger("dailyGain");

                inner.put(vId, r);
            }

            map.put(pId, inner);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList players = new NBTTagList();

        for (Map.Entry<UUID, Map<UUID, Record>> e : map.entrySet()) {
            NBTTagCompound pTag = new NBTTagCompound();
            pTag.setString("player", e.getKey().toString());

            NBTTagList villagers = new NBTTagList();
            for (Map.Entry<UUID, Record> ve : e.getValue().entrySet()) {
                NBTTagCompound vTag = new NBTTagCompound();
                vTag.setString("villager", ve.getKey().toString());
                vTag.setInteger("fam", ve.getValue().familiarity);
                vTag.setLong("day", ve.getValue().lastDay);
                vTag.setInteger("dailyGain", ve.getValue().dailyGain);
                villagers.appendTag(vTag);
            }

            pTag.setTag("villagers", villagers);
            players.appendTag(pTag);
        }

        nbt.setTag("players", players);
    }

    public static int getFamiliarity(EntityPlayer player, EntityVillager villager) {
        FamiliarityData data = get(player.worldObj);
        Map<UUID, Record> inner = data.map.get(player.getUniqueID());
        if (inner == null) return 0;

        Record r = inner.get(villager.getUniqueID());
        return r == null ? 0 : r.familiarity;
    }

    public static int increaseFamiliarity(EntityPlayer player, EntityVillager villager, int amount) {
        FamiliarityData data = get(player.worldObj);

        UUID p = player.getUniqueID();
        UUID v = villager.getUniqueID();

        Map<UUID, Record> inner = data.map.get(p);
        if (inner == null) {
            inner = new HashMap<>();
            data.map.put(p, inner);
        }

        Record r = inner.get(v);
        if (r == null) {
            r = new Record();
            inner.put(v, r);
        }

        long day = player.worldObj.getWorldTime() / 24000L;
        if (r.lastDay != day) {
            r.lastDay = day;
            r.dailyGain = 0;
        }

        int cap = CozyConfig.maxTalkFamiliarityPerDay;
        int allowed = cap - r.dailyGain;
        int inc = Math.max(0, Math.min(amount, allowed));

        r.familiarity = clamp(r.familiarity + inc, 0, 100);
        r.dailyGain += inc;

        data.markDirty();
        return r.familiarity;
    }

    private static FamiliarityData get(World world) {
        World storageWorld = world;

        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                World overworld = server.worldServerForDimension(0);
                if (overworld != null) storageWorld = overworld;
            }
        } catch (Throwable ignored) {}

        MapStorage storage = storageWorld.getPerWorldStorage();
        FamiliarityData data = (FamiliarityData) storage.loadData(FamiliarityData.class, IDENTIFIER);
        if (data == null) {
            data = new FamiliarityData();
            storage.setData(IDENTIFIER, data);
        }
        return data;
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static class Record {
        int familiarity = 0;
        long lastDay = -1;
        int dailyGain = 0;
    }
}
