package de.robotricker.transportpipes.config;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class LangConf extends Conf {

    private static LangConf langConf;

    public LangConf(Plugin configPlugin, String language) {
        super(configPlugin, "lang_" + language.toLowerCase(Locale.ENGLISH) + ".yml", "lang.yml");
        langConf = this;
    }

    public enum Key {

        PIPES_PIPE("pipes.pipe"),
        PIPES_ICE("pipes.ice"),
        PIPES_GOLDEN("pipes.golden"),
        PIPES_IRON("pipes.iron"),
        PIPES_VOID("pipes.void"),
        PIPES_EXTRACTION("pipes.extraction"),
        PIPES_CRAFTING("pipes.crafting"),
        WRENCH("wrench"),
        COLORS_WHITE("colors.white"),
        COLORS_YELLOW("colors.yellow"),
        COLORS_GREEN("colors.green"),
        COLORS_BLUE("colors.blue"),
        COLORS_RED("colors.red"),
        COLORS_BLACK("colors.black");

        private String key;

        Key(String key) {
            this.key = key;
        }

        public String get() {
            String value = (String) langConf.read(key);
            if (value == null) {
                value = "&cMissing Language Entry: &4" + key + "&r";
            }
            return ChatColor.translateAlternateColorCodes('&', value);
        }

    }

}
