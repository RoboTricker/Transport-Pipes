package de.robotricker.transportpipes.rendersystem.modelled;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.PipeModel;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeMidModelData;

public abstract class ModelledPipeModel extends PipeModel {

	protected static final ItemStack ITEM_HOE_MID_GOLDEN = createToolItemStack(13);
	protected static final ItemStack ITEM_HOE_MID_IRON = createToolItemStack(20);
	protected static final ItemStack ITEM_HOE_CONN_IRON_CLOSED = createToolItemStack(21);
	protected static final ItemStack ITEM_HOE_CONN_IRON_OPENED = createToolItemStack(22);
	protected static final ItemStack ITEM_HOE_MID_ICE = createToolItemStack(23);
	protected static final ItemStack ITEM_HOE_CONN_ICE = createToolItemStack(24);
	protected static final ItemStack ITEM_HOE_MID_VOID = createToolItemStack(35);
	protected static final ItemStack ITEM_HOE_CONN_VOID = createToolItemStack(36);

	public abstract ArmorStandData createMidASD(ModelledPipeMidModelData data);

	public abstract ArmorStandData createConnASD(ModelledPipeConnModelData data);

	public static ItemStack createToolItemStack(int damage) {
		ItemStack is = new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage);
		ItemMeta im = is.getItemMeta();
		im.setUnbreakable(true);
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is.setItemMeta(im);
		return is;
	}

}
