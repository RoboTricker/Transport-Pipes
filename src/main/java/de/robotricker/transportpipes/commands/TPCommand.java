package de.robotricker.transportpipes.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ThreadService;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.inventory.CreativeInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.utils.MessageUtils;

@CommandAlias("tpipes|transportpipes|transportpipe|tpipe|pipes|pipe")
public class TPCommand extends BaseCommand {

    @Inject
    private ThreadService threadService;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private GlobalDuctManager globalDuctManager;
    @Inject
    private ProtocolService protocol;
    @Inject
    private CreativeInventory creativeDuctInv;
    @Inject
    private DuctRegister ductRegister;

    @Subcommand("tps")
    @CommandPermission("transportpipes.tps")
    @Description("Shows some basic information about the plugin and it's runtime")
    public void onTPS(CommandSender cs) {
        int tps = threadService.getCurrentTPS();
        int pref_tps = threadService.getPreferredTPS();
        ChatColor tpsColor = ChatColor.DARK_GREEN;
        if (tps <= 1) {
            tpsColor = ChatColor.DARK_RED;
        } else if (tps <= 3) {
            tpsColor = ChatColor.RED;
        } else if (tps <= 4) {
            tpsColor = ChatColor.GOLD;
        } else if (tps <= 5) {
            tpsColor = ChatColor.GREEN;
        }

        cs.sendMessage(MessageUtils.formatColoredMsg("&6TransportPipes &7v" + plugin.getDescription().getVersion()));
        cs.sendMessage(MessageUtils.formatColoredMsg("&6TPS: " + tpsColor + tps + " &6/ &2" + pref_tps));

        for (World world : Bukkit.getWorlds()) {
            int worldPipes = 0;
            int worldItems = 0;
            Map<BlockLocation, Duct> ductMap = globalDuctManager.getDucts(world);
            synchronized (ductMap) {
                for (Duct duct : ductMap.values()) {
                    if (duct.getDuctType().getBaseDuctType().is("Pipe")) {
                        Pipe pipe = (Pipe) duct;
                        worldPipes++;
                        worldItems += 0;
                    }
                }
            }
            cs.sendMessage(MessageUtils.formatColoredMsg("&6" + world.getName() + ": &e" + worldPipes + " &6" + "pipes, &e" + worldItems + " &6items"));
        }
    }

    @Subcommand("rendersystem|rs|render")
    @Syntax("<baseDuctType> [rendersystem]")
    @CommandCompletion("@baseDuctType @nothing")
    public void onChangeRenderSystem(Player p, String baseDuctType, @Optional String renderSystem) {
        BaseDuctType<? extends Duct> bdt = ductRegister.baseDuctTypeOf(baseDuctType);
        if (bdt == null) {
            p.sendMessage(MessageUtils.formatColoredMsg("&4BaseDuctType does not exist"));
            return;
        }
        if (renderSystem == null) {
            p.sendMessage(MessageUtils.formatColoredMsg("&6Possible Render Systems:"));
            for (RenderSystem rs : new ArrayList<>(bdt.getRenderSystems())) {
                String suffix = rs.getCurrentPlayers().contains(p) ? " &6(active)" : "";
                p.sendMessage(MessageUtils.formatColoredMsg(" &b" + rs.getDisplayName() + suffix));
            }
        } else {
            for (RenderSystem newRs : new ArrayList<>(bdt.getRenderSystems())) {
                if (newRs.getDisplayName().equalsIgnoreCase(renderSystem)) {
                    if (globalDuctManager.getPlayerRenderSystem(p, bdt) == newRs) {
                        p.sendMessage(MessageUtils.formatColoredMsg("&4This rendersystem is already active"));
                        return;
                    }

                    //switch rendersystem
                    RenderSystem oldRs = globalDuctManager.getPlayerRenderSystem(p, bdt);
                    oldRs.getCurrentPlayers().remove(p);
                    synchronized (globalDuctManager.getPlayerDucts(p)) {
                        Iterator<Duct> ductIt = globalDuctManager.getPlayerDucts(p).iterator();
                        while (ductIt.hasNext()) {
                            Duct nextDuct =  ductIt.next();
                            if(nextDuct.getDuctType().getBaseDuctType().equals(bdt)){
                                protocol.removeASD(p, oldRs.getASDForDuct(nextDuct));
                                ductIt.remove();
                            }
                        }
                    }
                    newRs.getCurrentPlayers().add(p);

                    p.sendMessage(MessageUtils.formatColoredMsg("&6You've switched to the &b" + newRs.getDisplayName() + " &6render system"));
                    return;
                }
            }
            p.sendMessage(MessageUtils.formatColoredMsg("&4This render system does not exist"));
        }
    }

    @Subcommand("creative")
    public void onCreativeDuctInv(Player p) {
        creativeDuctInv.openInv(p);
    }

    @HelpCommand
    @Syntax("[command]")
    public void onDefault(CommandSender cs, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("reload")
    @CommandPermission("transportpipes.reload")
    public class ReloadCommand extends BaseCommand {

        @Subcommand("config")
        @CommandPermission("transportpipes.reload.config")
        @Description("Reloads the config")
        public void onReloadConfig(CommandSender cs) {
            cs.sendMessage("reload config");
        }

        @Subcommand("ducts|pipes")
        @CommandPermission("transportpipes.reload.ducts")
        @Description("Reloads all of the ducts")
        public void onReloadDucts(CommandSender cs) {
            cs.sendMessage("reload ducts");
        }

    }

}
