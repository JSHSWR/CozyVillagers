package com.koen.cozyvillagers.client.gui;

import com.koen.cozyvillagers.network.PacketHandler;
import com.koen.cozyvillagers.network.packet.PacketDialogueResponse;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiDialogue extends GuiScreen {
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 180;
    private static final int CLOSE_ID = 999;

    private static final Map<String, GuiDialogue> OPEN = new HashMap<>();

    private final String villagerId;

    private String villagerName;
    private String profession;
    private int tier;
    private String text;
    private List<String> responses;

    private int familiarity;
    private boolean waiting = false;

    public GuiDialogue(String villagerId, String villagerName, String profession,
                       int tier, String text, List<String> responses, int familiarity) {
        this.villagerId = villagerId;
        this.villagerName = villagerName;
        this.profession = profession;
        this.tier = tier;
        this.text = text;
        this.responses = new ArrayList<>(responses);
        this.familiarity = familiarity;
    }

    @Override
    public void initGui() {
        OPEN.put(villagerId, this);

        this.buttonList.clear();
        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = (this.width - buttonWidth) / 2;
        int yStart = (this.height - GUI_HEIGHT) / 2 + 108;

        if (responses == null || responses.isEmpty()) {
            // Conversation ended -> show a single close button
            // Localize the goodbye button.
            GuiButton close = new GuiButton(CLOSE_ID, x, yStart, buttonWidth, buttonHeight, net.minecraft.client.resources.I18n.format("gui.goodbye"));
            close.enabled = !waiting;
            this.buttonList.add(close);
            return;
        }

        for (int i = 0; i < responses.size(); i++) {
            GuiButton b = new GuiButton(i, x, yStart + i * (buttonHeight + 4), buttonWidth, buttonHeight, responses.get(i));
            b.enabled = !waiting;
            this.buttonList.add(b);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (waiting) return;

        if (button.id == CLOSE_ID) {
            this.mc.displayGuiScreen(null);
            return;
        }

        waiting = true;
        for (Object o : this.buttonList) {
            ((GuiButton) o).enabled = false;
        }

        PacketHandler.INSTANCE.sendToServer(new PacketDialogueResponse(villagerId, button.id));
        // Do NOT close the GUI; server will send PacketUpdateDialogue to update it.
    }

    @Override
    public void onGuiClosed() {
        OPEN.remove(villagerId);
        super.onGuiClosed();
    }

    public static void applyUpdate(String villagerId, String villagerName, String profession,
                                   int tier, String text, List<String> responses, int familiarity) {
        GuiDialogue gui = OPEN.get(villagerId);
        if (gui == null) return;

        gui.villagerName = villagerName;
        gui.profession = profession;
        gui.tier = tier;
        gui.text = text;
        gui.responses = (responses == null) ? new ArrayList<>() : new ArrayList<>(responses);
        gui.familiarity = familiarity;

        gui.waiting = false;
        gui.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        drawRect(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xAA000000);

        String title = villagerName + " the " + profession;
        this.fontRendererObj.drawStringWithShadow(title, guiLeft + 10, guiTop + 10, 0xFFFFFF);

        String tierName;
        switch (tier) {
            case 0: tierName = I18n.format("tier.stranger"); break;
            case 1: tierName = I18n.format("tier.neighbour"); break;
            case 2: tierName = I18n.format("tier.friend"); break;
            case 3: tierName = I18n.format("tier.bestie"); break;
            default: tierName = "Unknown";
        }
        this.fontRendererObj.drawString("Tier: " + tierName, guiLeft + 10, guiTop + 26, 0xCCCCCC);

        // Familiarity bar
        int barWidth = 180;
        int barHeight = 6;
        int filled = (int) (barWidth * (Math.max(0, Math.min(100, familiarity)) / 100.0f));
        int barX = guiLeft + 10;
        int barY = guiTop + 40;
        drawRect(barX, barY, barX + barWidth, barY + barHeight, 0xFF444444);
        drawRect(barX, barY, barX + filled, barY + barHeight, 0xFF00AA00);
        this.fontRendererObj.drawString(familiarity + "/100", barX + barWidth + 6, barY - 2, 0xAAAAAA);

        // Dialogue text
        int textX = guiLeft + 10;
        int textY = guiTop + 58;
        int maxWidth = GUI_WIDTH - 20;
        drawSplit(text, textX, textY, maxWidth, 0xFFFFFF);

        if (waiting) {
            this.fontRendererObj.drawStringWithShadow("...", guiLeft + GUI_WIDTH - 22, guiTop + 10, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSplit(String str, int x, int y, int width, int color) {
        List<String> lines = this.fontRendererObj.listFormattedStringToWidth(str, width);
        for (int i = 0; i < lines.size(); i++) {
            this.fontRendererObj.drawString(lines.get(i), x, y + i * this.fontRendererObj.FONT_HEIGHT, color);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
