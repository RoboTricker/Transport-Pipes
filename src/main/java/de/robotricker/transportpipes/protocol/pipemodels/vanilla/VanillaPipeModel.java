package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.pipemodels.PipeModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.utils.VanillaPipeModelData;

public abstract class VanillaPipeModel extends PipeModel {

	protected static final ItemStack ITEM_BLAZE = new ItemStack(Material.BLAZE_ROD);
	protected static final ItemStack ITEM_GOLD_BLOCK = new ItemStack(Material.GOLD_BLOCK);
	protected static final ItemStack ITEM_IRON_BLOCK = new ItemStack(Material.IRON_BLOCK);
	protected static final ItemStack ITEM_ICE_BLOCK = new ItemStack(Material.ICE);
	protected static final ItemStack ITEM_CARPET_WHITE = new ItemStack(Material.CARPET, 1, (short) 0);
	protected static final ItemStack ITEM_CARPET_YELLOW = new ItemStack(Material.CARPET, 1, (short) 4);
	protected static final ItemStack ITEM_CARPET_GREEN = new ItemStack(Material.CARPET, 1, (short) 5);
	protected static final ItemStack ITEM_CARPET_BLUE = new ItemStack(Material.CARPET, 1, (short) 11);
	protected static final ItemStack ITEM_CARPET_RED = new ItemStack(Material.CARPET, 1, (short) 14);
	protected static final ItemStack ITEM_CARPET_BLACK = new ItemStack(Material.CARPET, 1, (short) 15);

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

	protected AxisAlignedBB aabb;

	public abstract List<ArmorStandData> createASD(VanillaPipeModelData data);

	public AxisAlignedBB getAABB() {
		return aabb;
	}

	private static ItemStack createNamedItemStack(Material type, String name) {
		ItemStack is = new ItemStack(type, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}

}
