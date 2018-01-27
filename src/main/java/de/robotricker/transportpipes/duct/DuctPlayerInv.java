package de.robotricker.transportpipes.duct;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import de.robotricker.transportpipes.utils.config.LocConf;

public abstract class DuctPlayerInv extends DuctInv {

	protected Map<Player, Inventory> playerInvs;
	protected boolean onlyOnePlayerAtATime;

	public DuctPlayerInv(Duct duct, boolean onlyOnePlayerAtATime) {
		super(duct);
		this.playerInvs = new HashMap<>();
		this.onlyOnePlayerAtATime = onlyOnePlayerAtATime;
	}

	protected abstract Inventory openCustomInventory(Player p);

	@Override
	public void openOrUpdateInventory(Player p) {
		if (!playerInvs.containsKey(p)) {
			if (onlyOnePlayerAtATime && !playerInvs.isEmpty()) {
				p.sendMessage(LocConf.load(LocConf.MESSAGE_ALREADYOPENED));
				return;
			}
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
