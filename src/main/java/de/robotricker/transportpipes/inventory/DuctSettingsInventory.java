package de.robotricker.transportpipes.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.items.ItemService;

public abstract class DuctSettingsInventory extends GlobalInventory implements Listener {

    @Inject
    protected ItemService itemService;

    @Inject
    protected TransportPipes transportPipes;

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

    protected boolean drag(Player p, Set<Integer> rawSlots, DragType dragType) {
        return false;
    }

    protected boolean collect_to_cursor(Player p, ItemStack cursorItem, int rawSlot) {
        return false;
    }

    public abstract void save(Player p);

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getInventory() != null && e.getInventory().equals(inv) && e.getWhoClicked() instanceof Player) {
            if (itemService.isItemWildcardOrBarrier(e.getCurrentItem())) {
                e.setCancelled(true);
                return;
            }
            //don't call click method when double clicked, because before a double click, left click was already registered twice
            if (e.getClick() != ClickType.DOUBLE_CLICK && click((Player) e.getWhoClicked(), e.getRawSlot(), e.getClick())) {
                e.setCancelled(true);
            } else if (e.getClick() == ClickType.DOUBLE_CLICK && e.getAction() == InventoryAction.COLLECT_TO_CURSOR && collect_to_cursor((Player) e.getWhoClicked(), e.getCursor(), e.getRawSlot())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory() != null && e.getInventory().equals(inv) && e.getWhoClicked() instanceof Player) {
            if (drag((Player) e.getWhoClicked(), e.getRawSlots(), e.getType())) {
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
