package com.koen.cozyvillagers.dialogue;

import com.koen.cozyvillagers.data.FamiliarityData;
import com.koen.cozyvillagers.favors.FavorManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;

import java.util.ArrayList;
import java.util.List;

public class DialogueManager {
    public static class DialogueReply {
        public final String villagerName;
        public final String profession;
        public final int tier;
        public final String text;
        public final List<String> responses;
        public final int familiarity;

        public DialogueReply(String villagerName, String profession, int tier, String text, List<String> responses, int familiarity) {
            this.villagerName = villagerName;
            this.profession = profession;
            this.tier = tier;
            this.text = text;
            this.responses = responses;
            this.familiarity = familiarity;
        }
    }

    public static DialogueReply openConversation(EntityPlayer player, EntityVillager villager) {
        int familiarity = FamiliarityData.getFamiliarity(player, villager);
        int tier = tierOf(familiarity);

        String prof = professionName(villager.getProfession());
        String name = villager.getCustomNameTag();
        if (name == null || name.isEmpty()) name = "Villager";

        long t = villager.worldObj.getWorldTime() % 24000L;
        String tod = (t < 6000) ? "morning" : (t < 12000) ? "afternoon" : (t < 18000) ? "evening" : "night";

        String text = greeting(name, prof, tier, tod);

        List<String> responses = new ArrayList<>();
        responses.add("How are you?");
        responses.add("Any work for me?");
        if (tier >= 1) responses.add("Tell me about the village.");
        if (tier >= 2) responses.add("Any gossip?");

        return new DialogueReply(name, prof, tier, text, responses, familiarity);
    }

    public static DialogueReply handleResponse(EntityPlayer player, EntityVillager villager, int index) {
        int fam = FamiliarityData.increaseFamiliarity(player, villager, 1);
        int tier = tierOf(fam);

        String prof = professionName(villager.getProfession());
        String name = villager.getCustomNameTag();
        if (name == null || name.isEmpty()) name = "Villager";

        String closing;
        switch (index) {
            case 0: closing = "I'm doing well, thank you."; break;
            case 1:
                FavorManager.assignFavor(player, villager);
                closing = "Let me think… I might have a task for you."; break;
            case 2: closing = "The village has been peaceful lately."; break;
            case 3: closing = "No gossip at the moment."; break;
            default: closing = "See you around.";
        }

        return new DialogueReply(name, prof, tier, closing, new ArrayList<String>(), fam);
    }

    private static int tierOf(int f) {
        if (f < 20) return 0;
        if (f < 50) return 1;
        if (f < 80) return 2;
        return 3;
    }

    private static String professionName(int id) {
        switch (id) {
            case 0: return "Farmer";
            case 1: return "Librarian";
            case 2: return "Priest";
            case 3: return "Blacksmith";
            case 4: return "Butcher";
            default: return "Villager";
        }
    }

    private static String greeting(String name, String prof, int tier, String tod) {
        if (tier == 0) return "Good " + tod + ", stranger. I'm " + name + ", the " + prof.toLowerCase() + ".";
        if (tier == 1) return "Good " + tod + ", neighbor! It's nice to see you.";
        if (tier == 2) return "Good " + tod + ", friend. How can I help you today?";
        return "Good " + tod + ", bestie! What brings you by?";
    }
}