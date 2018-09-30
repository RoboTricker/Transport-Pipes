package de.robotricker.transportpipes.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String wrapColoredMsg(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
