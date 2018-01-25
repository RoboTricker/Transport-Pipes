package de.robotricker.transportpipes.rendersystem.modelled;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.Model;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeMidModelData;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public abstract class ModelledPipeModel extends Model {

	protected static final ItemStack ITEM_HOE_MID_GOLDEN = InventoryUtils.createToolItemStack(13);
	protected static final ItemStack ITEM_HOE_MID_IRON = InventoryUtils.createToolItemStack(20);
	protected static final ItemStack ITEM_HOE_CONN_IRON_CLOSED = InventoryUtils.createToolItemStack(21);
	protected static final ItemStack ITEM_HOE_CONN_IRON_OPENED = InventoryUtils.createToolItemStack(22);
	protected static final ItemStack ITEM_HOE_MID_ICE = InventoryUtils.createToolItemStack(23);
	protected static final ItemStack ITEM_HOE_CONN_ICE = InventoryUtils.createToolItemStack(24);
	protected static final ItemStack ITEM_HOE_MID_VOID = InventoryUtils.createToolItemStack(35);
	protected static final ItemStack ITEM_HOE_CONN_VOID = InventoryUtils.createToolItemStack(36);
	protected static final ItemStack ITEM_HOE_MID_EXTRACTION = InventoryUtils.createToolItemStack(37);
	protected static final ItemStack ITEM_HOE_CONN_EXTRACTION_CLOSED = InventoryUtils.createToolItemStack(38);
	protected static final ItemStack ITEM_HOE_CONN_EXTRACTION_OPENED = InventoryUtils.createToolItemStack(39);
	protected static final ItemStack ITEM_HOE_MID_CRAFTING = InventoryUtils.createToolItemStack(42);
	protected static final ItemStack ITEM_HOE_CONN_CRAFTING = InventoryUtils.createToolItemStack(43);

	public abstract ArmorStandData createMidASD(ModelledPipeMidModelData data);

	public abstract ArmorStandData createConnASD(ModelledPipeConnModelData data);

}
