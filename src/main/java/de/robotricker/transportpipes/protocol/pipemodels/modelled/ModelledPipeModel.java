package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.protocol.pipemodels.PipeModel;

public abstract class ModelledPipeModel extends PipeModel{

	protected static final ItemStack ITEM_HOE_MID_ICE = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_ICE = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_MID_GOLDEN = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_GOLDEN = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_MID_IRON = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_IRON = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_CONN_IRON_OUTPUT = new ItemStack(Material.WOOD_HOE, 1, (short) 12);

}
