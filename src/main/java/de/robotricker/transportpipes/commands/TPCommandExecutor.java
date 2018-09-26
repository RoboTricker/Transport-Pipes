package de.robotricker.transportpipes.commands;

import org.bukkit.command.CommandSender;

public interface TPCommandExecutor {

    boolean onTPCommand(CommandSender cs, String[] args);

}
