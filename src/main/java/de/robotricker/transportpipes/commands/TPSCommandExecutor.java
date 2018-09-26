package de.robotricker.transportpipes.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Map;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.utils.BlockLoc;

public class TPSCommandExecutor implements TPCommandExecutor {

    @Override
    public boolean onTPCommand(CommandSender cs, String[] args) {

        int tps = TransportPipes.instance.getTPThread().getCurrentTPS();
        int pref_tps = TransportPipes.instance.getTPThread().getPreferredTPS();
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

        cs.sendMessage(TransportPipes.wrapColoredMsg("&6TransportPipes &7v" + TransportPipes.instance.getDescription().getVersion()));
        cs.sendMessage(TransportPipes.wrapColoredMsg("&6TPS: " + tpsColor + tps + " &6/ &2" + pref_tps));

        for (World world : Bukkit.getWorlds()) {
            int worldPipes = 0;
            int worldItems = 0;
            Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctManager().getDucts(world);
            synchronized (ductMap) {
                for (Duct duct : ductMap.values()) {
                    if (duct.getDuctType().getBasicDuctType().is("Pipe")) {
                        Pipe pipe = (Pipe) duct;
                        worldPipes++;
                        worldItems += 0;
                    }
                }
            }
            cs.sendMessage(TransportPipes.wrapColoredMsg("&6" + world.getName() + ": &e" + worldPipes + " &6" + "pipes, &e" + worldItems + " &6items"));
        }

        return true;
    }
}
