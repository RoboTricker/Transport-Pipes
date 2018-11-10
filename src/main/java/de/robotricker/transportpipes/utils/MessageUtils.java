package de.robotricker.transportpipes.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String formatColoredMsg(String msg, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', String.format(msg, args));
    }

}
