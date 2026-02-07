package com.koen.cozyvillagers.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class CozyConfig {
    private static Configuration config;

    public static boolean enableDialogue = true;
    public static boolean enableFavors = true;
    public static boolean enableBubbles = true;

    public static double bubbleChancePerMinute = 0.2;
    public static int aiNudgeIntervalSeconds = 15;
    public static int maxTalkFamiliarityPerDay = 10;

    public static void init(File file) {
        config = new Configuration(file);
        sync();
    }

    public static void sync() {
        try {
            config.load();
            String cat = Configuration.CATEGORY_GENERAL;
            enableDialogue = config.getBoolean("enableDialogue", cat, enableDialogue, "Enable dialogue on non-sneak right-click.");
            enableFavors = config.getBoolean("enableFavors", cat, enableFavors, "Enable favors.");
            enableBubbles = config.getBoolean("enableBubbles", cat, enableBubbles, "Enable chat bubbles.");

            bubbleChancePerMinute = config.getFloat("bubbleChancePerMinute", cat, (float) bubbleChancePerMinute, 0F, 60F, "Bubble chance per minute (low default).");
            aiNudgeIntervalSeconds = config.getInt("aiNudgeIntervalSeconds", cat, aiNudgeIntervalSeconds, 5, 600, "AI nudge interval seconds (low frequency).");
            maxTalkFamiliarityPerDay = config.getInt("maxTalkFamiliarityPerDay", cat, maxTalkFamiliarityPerDay, 0, 100, "Daily talk familiarity cap.");
        } finally {
            if (config.hasChanged()) config.save();
        }
    }
}