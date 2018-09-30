package de.robotricker.transportpipes.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

import javax.inject.Inject;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import de.robotricker.transportpipes.DuctService;
import de.robotricker.transportpipes.TPThread;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.utils.MessageUtils;

@CommandAlias("tpipes|transportpipes|transportpipe|tpipe|pipes|pipe")
public class TPCommand extends BaseCommand {

    @Inject
    private TPThread tpThread;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private DuctService ductService;

    @Subcommand("tps")
    @CommandPermission("transportpipes.tps")
    @Description("Shows some basic information about the plugin and it's runtime")
    public void onTPS(CommandSender cs) {
        int tps = tpThread.getCurrentTPS();
        int pref_tps = tpThread.getPreferredTPS();
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

        cs.sendMessage(MessageUtils.wrapColoredMsg("&6TransportPipes &7v" + plugin.getDescription().getVersion()));
        cs.sendMessage(MessageUtils.wrapColoredMsg("&6TPS: " + tpsColor + tps + " &6/ &2" + pref_tps));

        for (World world : Bukkit.getWorlds()) {
            int worldPipes = 0;
            int worldItems = 0;
            Map<BlockLocation, Duct> ductMap = ductService.getDucts(world);
            synchronized (ductMap) {
                for (Duct duct : ductMap.values()) {
                    if (duct.getDuctType().getBaseDuctType().is("Pipe")) {
                        Pipe pipe = (Pipe) duct;
                        worldPipes++;
                        worldItems += 0;
                    }
                }
            }
            cs.sendMessage(MessageUtils.wrapColoredMsg("&6" + world.getName() + ": &e" + worldPipes + " &6" + "pipes, &e" + worldItems + " &6items"));
        }
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

    @HelpCommand
    @Syntax("[command]")
    public void onDefault(CommandSender cs, CommandHelp help) {
        help.showHelp();
    }

}
