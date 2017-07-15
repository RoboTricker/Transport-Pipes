package de.robotricker.transportpipes.pipes.colored;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeModel;

public enum PipeColor {

	WHITE("§f", (short) -1, (short) 1, (short) 7, (short) 15),
	BLUE("§1", (short) 11, (short) 2, (short) 8, (short) 4),
	RED("§4", (short) 14, (short) 3, (short) 9, (short) 1),
	YELLOW("§e", (short) 4, (short) 4, (short) 10, (short) 11),
	GREEN("§2", (short) 13, (short) 5, (short) 11, (short) 2),
	BLACK("§8", (short) 15, (short) 6, (short) 12, (short) 0);

	private String colorCode;
	private ItemStack vanillaModel_glassItem;
	private ItemStack modelledModel_midHoeItem;
	private ItemStack modelledModel_connHoeItem;
	private ItemStack dyeItem;

	private PipeColor(String colorCode, short glassMetadata, short midHoeMetadata, short connHoeMetadata, short dyeMetadata) {
		this.colorCode = colorCode;
		vanillaModel_glassItem = glassMetadata != -1 ? new ItemStack(Material.STAINED_GLASS, 1, glassMetadata) : new ItemStack(Material.GLASS);
		modelledModel_midHoeItem = ModelledPipeModel.createToolItemStack(midHoeMetadata);
		modelledModel_connHoeItem = ModelledPipeModel.createToolItemStack(connHoeMetadata);
		dyeItem = new ItemStack(Material.INK_SACK, 1, dyeMetadata);
	}

	public String getColorCode() {
		return colorCode;
	}

	public ItemStack getVanillaModel_GlassItem() {
		return vanillaModel_glassItem;
	}

	public ItemStack getModelledModel_MidHoeItem() {
		return modelledModel_midHoeItem;
	}

	public ItemStack getModelledModel_ConnHoeItem() {
		return modelledModel_connHoeItem;
	}

	public ItemStack getDyeItem() {
		return dyeItem;
	}

	public static PipeColor getPipeColorByPipeItem(ItemStack item) {
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().substring(2).equalsIgnoreCase(PipeType.COLORED.getFormattedPipeName())) {
			if (item != null) {
				for (PipeColor pipeColor : PipeColor.values()) {
					if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
						if (item.getItemMeta().getDisplayName().startsWith(pipeColor.getColorCode())) {
							return pipeColor;
						}
					}
				}
			}
		}
		return null;
	}

}
