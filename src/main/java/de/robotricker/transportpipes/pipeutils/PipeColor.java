package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;

public enum PipeColor {

	WHITE("§f", (short) -1, (short) 1, (short) 1, (short) 15),
	BLUE("§1", (short) 11, (short) 2, (short) 1, (short) 4),
	RED("§4", (short) 14, (short) 3, (short) 1, (short) 1),
	YELLOW("§e", (short) 4, (short) 4, (short) 1, (short) 11),
	GREEN("§2", (short) 13, (short) 5, (short) 1, (short) 2),
	BLACK("§0", (short) 15, (short) 6, (short) 1, (short) 0);

	private String colorCode;
	private ItemStack vanillaModel_glassItem;
	private ItemStack modelledModel_midHoeItem;
	private ItemStack modelledModel_connHoeItem;
	private ItemStack dyeItem;

	private PipeColor(String colorCode, short glassMetadata, short midHoeMetadata, short connHoeMetadata, short dyeMetadata) {
		this.colorCode = colorCode;
		vanillaModel_glassItem = glassMetadata != -1 ? new ItemStack(Material.STAINED_GLASS, 1, glassMetadata) : new ItemStack(Material.GLASS);
		modelledModel_midHoeItem = midHoeMetadata != -1 ? new ItemStack(Material.WOOD_HOE, 1, midHoeMetadata) : new ItemStack(Material.WOOD_HOE);
		modelledModel_midHoeItem.getItemMeta().setUnbreakable(true);
		modelledModel_connHoeItem = connHoeMetadata != -1 ? new ItemStack(Material.WOOD_HOE, 1, connHoeMetadata) : new ItemStack(Material.WOOD_HOE);
		modelledModel_connHoeItem.getItemMeta().setUnbreakable(true);
		dyeItem = new ItemStack(Material.INK_SACK, 1, dyeMetadata);
	}

	public String getColorCode() {
		return colorCode;
	}

	public ItemStack getVanillaModel_GlassItem() {
		return vanillaModel_glassItem;
	}
	
	public ItemStack getModelledModel_MidHoeItem(){
		return modelledModel_midHoeItem;
	}
	
	public ItemStack getModelledModel_ConnHoeItem(){
		return modelledModel_connHoeItem;
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
