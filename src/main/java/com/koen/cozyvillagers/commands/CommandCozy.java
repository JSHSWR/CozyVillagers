package com.koen.cozyvillagers.commands;

import com.koen.cozyvillagers.data.FamiliarityData;
import com.koen.cozyvillagers.favors.FavorManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Vec3;

import java.util.List;

public class CommandCozy extends CommandBase {

    @Override
    public String getCommandName() {
        return "cozy";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cozy <rep|favor>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) sender;

        if (args.length == 0) {
            player.addChatMessage(new ChatComponentTranslation(getCommandUsage(sender)));
            return;
        }

        if ("rep".equalsIgnoreCase(args[0])) {
            EntityVillager target = getLookingVillager(player, 6.0);
            if (target == null) {
                player.addChatMessage(new ChatComponentTranslation("command.cozy.rep.no_target"));
                return;
            }
            int fam = FamiliarityData.getFamiliarity(player, target);
            String tierKey = (fam < 20) ? "tier.stranger" : (fam < 50) ? "tier.neighbour" : (fam < 80) ? "tier.friend" : "tier.bestie";
            player.addChatMessage(new ChatComponentTranslation(
                    "command.cozy.rep.status",
                    target.getCustomNameTag(),
                    fam,
                    new ChatComponentTranslation(tierKey).getUnformattedText()
            ));
            return;
        }

        if ("favor".equalsIgnoreCase(args[0])) {
            String status = FavorManager.getFavorStatus(player);
            if (status == null) player.addChatMessage(new ChatComponentTranslation("command.cozy.favor.none"));
            else player.addChatMessage(new ChatComponentTranslation("command.cozy.favor.status", status));
            return;
        }

        player.addChatMessage(new ChatComponentTranslation(getCommandUsage(sender)));
    }

    private EntityVillager getLookingVillager(EntityPlayer player, double distance) {
        Vec3 eye = player.getPositionEyes(1.0f);
        Vec3 look = player.getLook(1.0f);

        List<EntityVillager> list = player.worldObj.getEntitiesWithinAABB(
                EntityVillager.class,
                player.getEntityBoundingBox()
                        .addCoord(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance)
                        .expand(1, 1, 1)
        );

        double best = distance * distance;
        EntityVillager result = null;

        for (EntityVillager v : list) {
            Vec3 vp = new Vec3(v.posX, v.posY + v.getEyeHeight(), v.posZ);
            double t = vp.subtract(eye).dotProduct(look);
            if (t < 0 || t > distance) continue;

            Vec3 proj = eye.addVector(look.xCoord * t, look.yCoord * t, look.zCoord * t);
            double d2 = proj.squareDistanceTo(vp);
            if (d2 < 1.0D && t * t < best) {
                best = t * t;
                result = v;
            }
        }
        return result;
    }
}