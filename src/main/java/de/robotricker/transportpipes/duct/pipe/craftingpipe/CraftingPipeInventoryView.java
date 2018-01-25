package de.robotricker.transportpipes.duct.pipe.craftingpipe;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class CraftingPipeInventoryView extends InventoryView {

	Player p;
	CraftingInventory craftingInventory;

	public CraftingPipeInventoryView(Player p, CraftingInventory craftingInventory) {
		super();
		this.p = p;
		this.craftingInventory = craftingInventory;
	}

	@Override
	public Inventory getTopInventory() {
		return craftingInventory;
	}

	@Override
	public Inventory getBottomInventory() {
		return p.getInventory();
	}

	@Override
	public HumanEntity getPlayer() {
		return p;
	}

	@Override
	public InventoryType getType() {
		return InventoryType.WORKBENCH;
	}

}
