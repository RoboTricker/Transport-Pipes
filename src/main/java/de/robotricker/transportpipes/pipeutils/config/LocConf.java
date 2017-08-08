package de.robotricker.transportpipes.pipeutils.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;

import de.robotricker.transportpipes.TransportPipes;

public class LocConf extends Conf {

	public static final String PIPES_COLORED = "pipes.colored";
	public static final String PIPES_ICE = "pipes.ice";
	public static final String PIPES_GOLDEN = "pipes.golden";
	public static final String PIPES_IRON = "pipes.iron";
	public static final String PIPES_VOID = "pipes.void";
	public static final String PIPES_EXTRACTION = "pipes.extraction";
	public static final String PIPES_WRENCH = "pipes.wrench";
	public static final String GOLDENPIPE_TITLE = "goldenpipe.title";
	public static final String GOLDENPIPE_COLORS_WHITE = "goldenpipe.colors.white";
	public static final String GOLDENPIPE_COLORS_YELLOW = "goldenpipe.colors.yellow";
	public static final String GOLDENPIPE_COLORS_GREEN = "goldenpipe.colors.green";
	public static final String GOLDENPIPE_COLORS_BLUE = "goldenpipe.colors.blue";
	public static final String GOLDENPIPE_COLORS_RED = "goldenpipe.colors.red";
	public static final String GOLDENPIPE_COLORS_BLACK = "goldenpipe.colors.black";
	public static final String GOLDENPIPE_FILTERING_FILTERBY_TYPE_DAMAGE = "goldenpipe.filtering.filterby_type_damage";
	public static final String GOLDENPIPE_FILTERING_FILTERBY_TYPE_NBT = "goldenpipe.filtering.filterby_type_nbt";
	public static final String GOLDENPIPE_FILTERING_FILTERBY_TYPE = "goldenpipe.filtering.filterby_type";
	public static final String GOLDENPIPE_FILTERING_FILTERBY_TYPE_DAMAGE_NBT = "goldenpipe.filtering.filterby_type_damage_nbt";
	public static final String GOLDENPIPE_FILTERING_BLOCKALL = "goldenpipe.filtering.blockall";
	public static final String GOLDENPIPE_FILTERING_CLICKTOCHANGE = "goldenpipe.filtering.clicktochange";
	public static final String EXTRACTIONPIPE_TITLE = "extractionpipe.title";
	public static final String EXTRACTIONPIPE_DIRECTION_TEXT = "extractionpipe.direction.text";
	public static final String EXTRACTIONPIPE_DIRECTION_DONTEXTRACT = "extractionpipe.direction.dontextract";
	public static final String EXTRACTIONPIPE_DIRECTION_CLICKTOCHANGE = "extractionpipe.direction.clicktochange";
	public static final String EXTRACTIONPIPE_CONDITION_NEEDSREDSTONE = "extractionpipe.condition.needsredstone";
	public static final String EXTRACTIONPIPE_CONDITION_ALWAYSEXTRACT = "extractionpipe.condition.alwaysextract";
	public static final String EXTRACTIONPIPE_CONDITION_NEVEREXTRACT = "extractionpipe.condition.neverextract";
	public static final String EXTRACTIONPIPE_CONDITION_CLICKTOCHANGE = "extractionpipe.condition.clicktochange";
	public static final String EXTRACTIONPIPE_AMOUNT_EXTRACT1 = "extractionpipe.amount.extract1";
	public static final String EXTRACTIONPIPE_AMOUNT_EXTRACT16 = "extractionpipe.amount.extract16";
	public static final String EXTRACTIONPIPE_AMOUNT_CLICKTOCHANGE = "extractionpipe.amount.clicktochange";
	public static final String SETTINGS_TITLE = "settings.title";
	public static final String SETTINGS_RENDERDISTANCE_TITLE = "settings.renderdistance.title";
	public static final String SETTINGS_RENDERDISTANCE_DECREASE = "settings.renderdistance.decrease";
	public static final String SETTINGS_RENDERDISTANCE_INCREASE = "settings.renderdistance.increase";
	public static final String SETTINGS_RENDERDISTANCE_DESCRIPTION = "settings.renderdistance.description";
	public static final String SETTINGS_RENDERSYSTEM_TITLE = "settings.rendersystem.title";
	public static final String SETTINGS_RENDERSYSTEM_DESCRIPTION = "settings.rendersystem.description";
	public static final String SETTINGS_RENDERSYSTEM_VANILLA = "settings.rendersystem.vanilla";
	public static final String SETTINGS_RENDERSYSTEM_MODELLED = "settings.rendersystem.modelled";
	public static final String COMMANDS_DESCRIPTION_SETTINGS = "commands.description.settings";
	public static final String COMMANDS_DESCRIPTION_TPS = "commands.description.tps";
	public static final String COMMANDS_DESCRIPTION_RELOAD = "commands.description.reload";
	public static final String COMMANDS_DESCRIPTION_UPDATE = "commands.description.update";
	public static final String COMMANDS_DESCRIPTION_CREATIVE = "commands.description.creative";
	public static final String COMMANDS_DESCRIPTION_SAVE = "command.description.save";
	public static final String COMMANDS_DESCRIPTION_DELETE = "command.description.delete";
	public static final String COMMANDS_HEADER = "commands.header";
	public static final String COMMANDS_FOOTER = "commands.footer";
	public static final String COMMANDS_NOPERM = "commands.noperm";
	public static final String CREATIVE_TITLE = "creative.title";

	public LocConf() {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "localization.yml"));
		saveAsDefault(PIPES_COLORED, "Pipe");
		saveAsDefault(PIPES_ICE, "Ice-Pipe");
		saveAsDefault(PIPES_GOLDEN, "Golden-Pipe");
		saveAsDefault(PIPES_IRON, "Iron-Pipe");
		saveAsDefault(PIPES_VOID, "Void-Pipe");
		saveAsDefault(PIPES_EXTRACTION, "Extraction-Pipe");
		saveAsDefault(PIPES_WRENCH, "Wrench");
		saveAsDefault(GOLDENPIPE_TITLE, "Golden-Pipe");
		saveAsDefault(GOLDENPIPE_COLORS_WHITE, "&fWhite");
		saveAsDefault(GOLDENPIPE_COLORS_YELLOW, "&eYellow");
		saveAsDefault(GOLDENPIPE_COLORS_GREEN, "&aGreen");
		saveAsDefault(GOLDENPIPE_COLORS_BLUE, "&9Blue");
		saveAsDefault(GOLDENPIPE_COLORS_RED, "&cRed");
		saveAsDefault(GOLDENPIPE_COLORS_BLACK, "&8Black");
		saveAsDefault(GOLDENPIPE_FILTERING_FILTERBY_TYPE, "&7Filter items by &cTYPE");
		saveAsDefault(GOLDENPIPE_FILTERING_FILTERBY_TYPE_DAMAGE, "&7Filter items by &cTYPE, DAMAGE");
		saveAsDefault(GOLDENPIPE_FILTERING_FILTERBY_TYPE_NBT, "&7Filter items by &cTYPE, ITEMMETA");
		saveAsDefault(GOLDENPIPE_FILTERING_FILTERBY_TYPE_DAMAGE_NBT, "&7Filter items by &cTYPE, DAMAGE, ITEMMETA");
		saveAsDefault(GOLDENPIPE_FILTERING_BLOCKALL, "&cBLOCK &7all items");
		saveAsDefault(GOLDENPIPE_FILTERING_CLICKTOCHANGE, "&7Click to change filtering mode.");
		saveAsDefault(EXTRACTIONPIPE_TITLE, "Extraction-Pipe");
		saveAsDefault(EXTRACTIONPIPE_DIRECTION_TEXT, "&7Extract Direction: &c%s");
		saveAsDefault(EXTRACTIONPIPE_DIRECTION_DONTEXTRACT, "&7Extract Direction: &cDONT EXTRACT");
		saveAsDefault(EXTRACTIONPIPE_DIRECTION_CLICKTOCHANGE, "&7Click to change extract direction");
		saveAsDefault(EXTRACTIONPIPE_CONDITION_NEEDSREDSTONE, "&7Extract Condition: &cNEEDS REDSTONE");
		saveAsDefault(EXTRACTIONPIPE_CONDITION_ALWAYSEXTRACT, "&7Extract Condition: &aALWAYS EXTRACT");
		saveAsDefault(EXTRACTIONPIPE_CONDITION_NEVEREXTRACT, "&7Extract Condition: &4NEVER EXTRACT");
		saveAsDefault(EXTRACTIONPIPE_CONDITION_CLICKTOCHANGE, "&7Click to change extract condition");
		saveAsDefault(EXTRACTIONPIPE_AMOUNT_EXTRACT1, "&7Extract Amount: &a1");
		saveAsDefault(EXTRACTIONPIPE_AMOUNT_EXTRACT16, "&7Extract Amount: &a16");
		saveAsDefault(EXTRACTIONPIPE_AMOUNT_CLICKTOCHANGE, "&7Click to change extract amount");
		saveAsDefault(SETTINGS_TITLE, "Player Settings");
		saveAsDefault(SETTINGS_RENDERDISTANCE_TITLE, "&6Render Distance: &e%d");
		saveAsDefault(SETTINGS_RENDERDISTANCE_DECREASE, "&6Decrease");
		saveAsDefault(SETTINGS_RENDERDISTANCE_INCREASE, "&6Increase");
		saveAsDefault(SETTINGS_RENDERDISTANCE_DESCRIPTION, Arrays.asList("&7The Render Distance represents", "&7the distance in blocks in which you", "&7can see pipes."));
		saveAsDefault(SETTINGS_RENDERSYSTEM_TITLE, "&6Your Pipe Render System: &b%s");
		saveAsDefault(SETTINGS_RENDERSYSTEM_DESCRIPTION, Arrays.asList("&7Click to switch between Vanilla", "&7and Modelled Render System.", "&7The Modelled Render System uses a resourcepack.", "&7The Vanilla Render System", "&7uses the Vanilla Minecraft textures."));
		saveAsDefault(SETTINGS_RENDERSYSTEM_VANILLA, "Vanilla");
		saveAsDefault(SETTINGS_RENDERSYSTEM_MODELLED, "Modelled");
		saveAsDefault(COMMANDS_DESCRIPTION_SETTINGS, "&eOpens a player-specific settings menu");
		saveAsDefault(COMMANDS_DESCRIPTION_TPS, "&eShows some general information about the pipes in all worlds and the ticks per second of the plugin thread");
		saveAsDefault(COMMANDS_DESCRIPTION_RELOAD, "&eReloads all pipes or the config");
		saveAsDefault(COMMANDS_DESCRIPTION_UPDATE, "&eChecks for a new plugin version at SpigotMC and updates the plugin if possible");
		saveAsDefault(COMMANDS_DESCRIPTION_CREATIVE, "&eOpens an inventory with every pipe inside it.");
		saveAsDefault(COMMANDS_DESCRIPTION_SAVE, "&eSaves all pipes in all worlds.");
		saveAsDefault(COMMANDS_DESCRIPTION_DELETE, "&eDelete all pipes with a distance to you smaller than \"Radius\".");
		saveAsDefault(COMMANDS_HEADER, "&7&l[ &6TransportPipes %s &7&l]");
		saveAsDefault(COMMANDS_FOOTER, "&7&l&m--------------------------------------");
		saveAsDefault(COMMANDS_NOPERM, "&4You don't have permission to perform this command.");
		saveAsDefault(CREATIVE_TITLE, "Pipe Overview");
		finishDefault();
	}

	public String get(String key) {
		return ChatColor.translateAlternateColorCodes('&', (String) read(key));
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String key) {
		List<String> list = (List<String>) read(key);
		for (int i = 0; i < list.size(); i++) {
			list.set(i, ChatColor.translateAlternateColorCodes('&', list.get(i)));
		}
		return list;
	}

	public static String load(String key) {
		return TransportPipes.instance.locConf.get(key);
	}

	public static List<String> loadStringList(String key) {
		return TransportPipes.instance.locConf.getStringList(key);
	}

}
