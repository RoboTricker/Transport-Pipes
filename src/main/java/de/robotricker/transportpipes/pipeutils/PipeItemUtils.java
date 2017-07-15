package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;

public class PipeItemUtils {

	public static final ItemStack ITEM_PIPE_WHITE = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_BLUE = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_RED = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_YELLOW = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_GREEN = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_BLACK = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_GOLDEN = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_IRON = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_PIPE_ICE = new ItemStack(Material.BLAZE_ROD);
	public static final ItemStack ITEM_WRENCH = new ItemStack(Material.STICK);

	public static ItemStack createNamedItemStack(Material type, String name) {
		ItemStack is = new ItemStack(type, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack createToolItemStack(int damage, String name) {
		ItemStack is = new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.setUnbreakable(true);
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is.setItemMeta(im);
		return is;
	}
	
	public static ItemStack changeDisplayName(ItemStack is, String displayName){
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(displayName);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack getPipeItem(PipeType pipeType, PipeColor pipeColor) {
		switch (pipeType) {
		case COLORED:
			switch (pipeColor) {
			case WHITE:
				return changeDisplayName(ITEM_PIPE_WHITE, PipeColor.WHITE.getColorCode() + PipeType.COLORED.getFormattedPipeName());
			case BLUE:
				return changeDisplayName(ITEM_PIPE_BLUE, PipeColor.BLUE.getColorCode() + PipeType.COLORED.getFormattedPipeName());
			case RED:
				return changeDisplayName(ITEM_PIPE_RED, PipeColor.RED.getColorCode() + PipeType.COLORED.getFormattedPipeName());
			case YELLOW:
				return changeDisplayName(ITEM_PIPE_YELLOW, PipeColor.YELLOW.getColorCode() + PipeType.COLORED.getFormattedPipeName());
			case GREEN:
				return changeDisplayName(ITEM_PIPE_GREEN, PipeColor.GREEN.getColorCode() + PipeType.COLORED.getFormattedPipeName());
			case BLACK:
				return changeDisplayName(ITEM_PIPE_BLACK, PipeColor.BLACK.getColorCode() + PipeType.COLORED.getFormattedPipeName());
			default:
				return null;
			}
		case GOLDEN:
			return changeDisplayName(ITEM_PIPE_GOLDEN, PipeType.GOLDEN.getFormattedPipeName());
		case IRON:
			return changeDisplayName(ITEM_PIPE_IRON, PipeType.IRON.getFormattedPipeName());
		case ICE:
			return changeDisplayName(ITEM_PIPE_ICE, PipeType.ICE.getFormattedPipeName());
		default:
			return null;
		}
	}

	public static ItemStack getWrenchItem() {
		return changeDisplayName(ITEM_WRENCH, TransportPipes.instance.getFormattedWrenchName());
	}

	public static boolean isItemStackWrench(ItemStack clickedItem) {
		return changeDisplayName(ITEM_WRENCH, TransportPipes.instance.getFormattedWrenchName()).isSimilar(clickedItem);
	}

}
