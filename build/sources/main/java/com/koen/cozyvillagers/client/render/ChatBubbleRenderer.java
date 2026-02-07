package com.koen.cozyvillagers.client.render;

import com.koen.cozyvillagers.config.CozyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatBubbleRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!CozyConfig.enableBubbles) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        RenderManager rm = mc.getRenderManager();
        FontRenderer fr = mc.fontRendererObj;

        double px = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double py = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double pz = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityVillager)) continue;
            EntityVillager v = (EntityVillager) obj;

            NBTTagCompound data = v.getEntityData();
            if (!data.hasKey("CozyBubbleText")) continue;

            long until = data.getLong("CozyBubbleUntil");
            long now = v.worldObj.getTotalWorldTime();
            if (now > until) continue;

            String text = data.getString("CozyBubbleText");
            if (text == null || text.isEmpty()) continue;

            double x = v.lastTickPosX + (v.posX - v.lastTickPosX) * event.partialTicks - px;
            double y = v.lastTickPosY + (v.posY - v.lastTickPosY) * event.partialTicks - py + v.height + 0.6;
            double z = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * event.partialTicks - pz;

            renderFloatingText(fr, rm, text, x, y, z);
        }
    }

    private void renderFloatingText(FontRenderer fr, RenderManager rm, String text, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(-rm.playerViewY, 0F, 1F, 0F);
        GlStateManager.rotate(rm.playerViewX, 1F, 0F, 0F);
        GlStateManager.scale(-0.016F, -0.016F, 0.016F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        int w = fr.getStringWidth(text) / 2;
        fr.drawString(text, -w, 0, 0xFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}