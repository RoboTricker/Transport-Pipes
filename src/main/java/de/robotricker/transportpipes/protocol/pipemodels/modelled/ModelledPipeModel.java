package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.pipemodels.PipeModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.utils.ModelledPipeMidModelData;

public abstract class ModelledPipeModel extends PipeModel {

	protected static final ItemStack ITEM_HOE_MID_GOLDEN = createSwordItemStack(13);
	protected static final ItemStack ITEM_HOE_MID_IRON = createSwordItemStack(20);
	protected static final ItemStack ITEM_HOE_CONN_IRON_CLOSED = createSwordItemStack(21);
	protected static final ItemStack ITEM_HOE_CONN_IRON_OPENED = createSwordItemStack(22);
	protected static final ItemStack ITEM_HOE_MID_ICE = createSwordItemStack(23);
	protected static final ItemStack ITEM_HOE_CONN_ICE = createSwordItemStack(24);

	public static final ItemStack ITEM_PIPE_WHITE = createSwordItemStack(25, PipeColor.WHITE.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_BLUE = createSwordItemStack(26, PipeColor.BLUE.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_RED = createSwordItemStack(27, PipeColor.RED.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_YELLOW = createSwordItemStack(28, PipeColor.YELLOW.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_GREEN = createSwordItemStack(29, PipeColor.GREEN.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_BLACK = createSwordItemStack(30, PipeColor.BLACK.getColorCode() + TransportPipes.instance.PIPE_NAME);
	public static final ItemStack ITEM_PIPE_GOLDEN = createSwordItemStack(31, TransportPipes.instance.GOLDEN_PIPE_NAME);
	public static final ItemStack ITEM_PIPE_IRON = createSwordItemStack(32, TransportPipes.instance.IRON_PIPE_NAME);
	public static final ItemStack ITEM_PIPE_ICE = createSwordItemStack(33, TransportPipes.instance.ICE_PIPE_NAME);
	public static final ItemStack ITEM_WRENCH = createSwordItemStack(34, TransportPipes.instance.WRENCH_NAME);

	public abstract ArmorStandData createMidASD(ModelledPipeMidModelData data);

	public abstract ArmorStandData createConnASD(ModelledPipeConnModelData data);

	public static ItemStack createSwordItemStack(int damage) {
		ItemStack is = new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage);
		ItemMeta im = is.getItemMeta();
		im.setUnbreakable(true);
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack createSwordItemStack(int damage, String name) {
		ItemStack is = new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.setUnbreakable(true);
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is.setItemMeta(im);
		return is;
	}

}
