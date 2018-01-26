package de.robotricker.transportpipes.duct;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class DuctSharedInv extends DuctInv {

	protected Inventory inventory;

	public DuctSharedInv(Duct duct, String invTitle, int invSize) {
		super(duct);
		this.inventory = Bukkit.createInventory(null, invSize, invTitle);
	}

	public Inventory getSharedInventory() {
		return inventory;
	}

	@Override
	public void openOrUpdateInventory(Player p) {
		populateInventory(p, inventory);
		p.openInventory(inventory);
	}

	@Override
	protected boolean containsInventory(Inventory inventory) {
		return inventory.equals(this.inventory);
	}

}
