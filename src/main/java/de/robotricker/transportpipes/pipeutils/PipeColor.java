package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;

public enum PipeColor {

	WHITE("§f", (short) -1, (short) 15),
	BLUE("§1", (short) 11, (short) 4),
	RED("§4", (short) 14, (short) 1),
	YELLOW("§e", (short) 4, (short) 11),
	GREEN("§2", (short) 13, (short) 2),
	BLACK("§0", (short) 15, (short) 0);

	private String colorCode;
	private ItemStack glassItem;
	private ItemStack dyeItem;

	private PipeColor(String colorCode, short glassMetadata, short dyeMetadata) {
		this.colorCode = colorCode;
		glassItem = glassMetadata != -1 ? new ItemStack(Material.STAINED_GLASS, 1, glassMetadata) : new ItemStack(Material.GLASS);
		dyeItem = new ItemStack(Material.INK_SACK, 1, dyeMetadata);
	}

	public String getColorCode() {
		return colorCode;
	}

	public ItemStack getGlassItem() {
		return glassItem;
	}

	public ItemStack getDyeItem() {
		return dyeItem;
	}

	public static PipeColor getPipeColorByPipeItem(ItemStack item) {
		if (item.getItemMeta().getDisplayName().contains(TransportPipes.instance.GOLDEN_PIPE_NAME) || item.getItemMeta().getDisplayName().contains(TransportPipes.instance.IRON_PIPE_NAME)) {
			return PipeColor.WHITE;
		}
		if (item != null) {
			for (PipeColor pipeColor : PipeColor.values()) {
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					if (item.getItemMeta().getDisplayName().startsWith(pipeColor.getColorCode())) {
						return pipeColor;
					}
				}
			}
		}
		return PipeColor.WHITE;
	}

}
