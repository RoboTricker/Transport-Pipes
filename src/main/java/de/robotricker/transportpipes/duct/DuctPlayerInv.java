package de.robotricker.transportpipes.duct;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public abstract class DuctPlayerInv extends DuctInv {

	protected Map<Player, Inventory> playerInvs;

	public DuctPlayerInv(Duct duct) {
		super(duct);
		this.playerInvs = new HashMap<>();
	}

	protected abstract Inventory openCustomInventory(Player p);

	@Override
	public void openOrUpdateInventory(Player p) {
		if (!playerInvs.containsKey(p)) {
			playerInvs.put(p, openCustomInventory(p));
		}
		populateInventory(p, playerInvs.get(p));
	}

	@Override
	protected void notifyInvSave(Player p, Inventory inventory) {
		if (playerInvs.containsKey(p)) {
			playerInvs.remove(p);
		}
	}
	
	@Override
	protected boolean containsInventory(Inventory inventory) {
		return playerInvs.containsValue(inventory);
	}

}
