package de.robotricker.transportpipes.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.items.ItemService;

public abstract class DuctSettingsInventory extends GlobalInventory implements Listener {

    @Inject
    protected ItemService itemService;

    protected Duct duct;

    public final void setDuct(Duct duct) {
        this.duct = duct;
    }

    public void closeForAllPlayers(TransportPipes transportPipes){
        transportPipes.runTaskSync(() -> {
            for (int i = 0; i < inv.getViewers().size(); i++) {
                inv.getViewers().get(i).closeInventory();
            }
        });
    }

    public abstract void create();

    public abstract void populate();

    /**
     * returns whether to cancel the click event or not
     */
    protected abstract boolean click(Player p, int rawSlot, ClickType ct);

    protected abstract void save(Player p);

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getInventory() != null && e.getInventory().equals(inv) && e.getWhoClicked() instanceof Player) {
            if (itemService.isItemGlassOrBarrier(e.getCurrentItem())) {
                e.setCancelled(true);
                return;
            }
            //don't call click method when double clicked, because before a double click, left click was already registered twice
            if (e.getClick() != ClickType.DOUBLE_CLICK && click((Player) e.getWhoClicked(), e.getRawSlot(), e.getClick())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory() != null && e.getInventory().equals(inv) && e.getPlayer() instanceof Player) {
            save((Player) e.getPlayer());
        }
    }

}
