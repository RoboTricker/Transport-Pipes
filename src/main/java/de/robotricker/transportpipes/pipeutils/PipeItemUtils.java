package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;

public class PipeItemUtils {

	public static final ItemStack ITEM_PIPE_WHITE = createNamedItemStack(Material.BLAZE_ROD, PipeColor.WHITE.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_BLUE = createNamedItemStack(Material.BLAZE_ROD, PipeColor.BLUE.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_RED = createNamedItemStack(Material.BLAZE_ROD, PipeColor.RED.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_YELLOW = createNamedItemStack(Material.BLAZE_ROD, PipeColor.YELLOW.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_GREEN = createNamedItemStack(Material.BLAZE_ROD, PipeColor.GREEN.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_BLACK = createNamedItemStack(Material.BLAZE_ROD, PipeColor.BLACK.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_GOLDEN = createNamedItemStack(Material.BLAZE_ROD, TransportPipes.instance.GOLDEN_PIPE_NAME);
	public static final ItemStack ITEM_PIPE_IRON = createNamedItemStack(Material.BLAZE_ROD, TransportPipes.instance.IRON_PIPE_NAME);
	public static final ItemStack ITEM_PIPE_ICE = createNamedItemStack(Material.BLAZE_ROD, TransportPipes.instance.ICE_PIPE_NAME);
	public static final ItemStack ITEM_WRENCH = createNamedItemStack(Material.STICK, TransportPipes.instance.WRENCH_NAME);

	private static ItemStack createNamedItemStack(Material type, String name) {
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

	public static ItemStack getPipeItem(PipeType pipeType, PipeColor pipeColor) {
		switch (pipeType) {
		case COLORED:
			switch (pipeColor) {
			case WHITE:
				return ITEM_PIPE_WHITE;
			case BLUE:
				return ITEM_PIPE_BLUE;
			case RED:
				return ITEM_PIPE_RED;
			case YELLOW:
				return ITEM_PIPE_YELLOW;
			case GREEN:
				return ITEM_PIPE_GREEN;
			case BLACK:
				return ITEM_PIPE_BLACK;
			default:
				return null;
			}
		case GOLDEN:
			return ITEM_PIPE_GOLDEN;
		case IRON:
			return ITEM_PIPE_IRON;
		case ICE:
			return ITEM_PIPE_ICE;
		default:
			return null;
		}
	}

	public static ItemStack getWrenchItem() {
		return ITEM_WRENCH;
	}

	public static boolean isItemStackWrench(ItemStack clickedItem) {
		return ITEM_WRENCH.isSimilar(clickedItem);
	}

}
