package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.ModelledRenderSystem;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.rendersystems.VanillaRenderSystem;

public class PlayerSettingsInventory extends IndividualInventory implements Listener {

    @Inject
    private ItemService itemService;
    @Inject
    private PlayerSettingsService playerSettingsService;
    @Inject
    private DuctRegister ductRegister;
    @Inject
    private GlobalDuctManager globalDuctManager;
    @Inject
    private ProtocolService protocolService;

    private Set<Inventory> inventories;

    public PlayerSettingsInventory() {
        inventories = new HashSet<>();
    }

    @Override
    Inventory create(Player p) {
        Inventory inv = Bukkit.createInventory(null, 2 * 9, "Player Settings");
        inventories.add(inv);

        PlayerSettingsConf playerSettingsConf = playerSettingsService.getOrCreateSettingsConf(p);

        ItemStack decreaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
        itemService.changeDisplayNameAndLore(decreaseBtn, "§6Decrease Render Distance");
        ItemStack increaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
        itemService.changeDisplayNameAndLore(increaseBtn, "§6Increase Render Distance");

        ItemStack eye = new ItemStack(Material.EYE_OF_ENDER, playerSettingsConf.getRenderDistance(), (short) 0);
        itemService.changeDisplayNameAndLore(eye, "§7Render Distance (in blocks): §c" + playerSettingsConf.getRenderDistance());

        ItemStack glassPane = itemService.createGlassItem(DyeColor.GRAY);

        itemService.populateInventoryLine(inv, 0, glassPane, glassPane, decreaseBtn, glassPane, eye, glassPane, increaseBtn, glassPane, glassPane);

        String renderSystemName = playerSettingsConf.getRenderSystemName();

        ItemStack renderSystemRepresentationItem = RenderSystem.getItem(renderSystemName, itemService, ductRegister);
        itemService.changeDisplayNameAndLore(renderSystemRepresentationItem, "§7Render System: §c" + renderSystemName);

        boolean showItems = playerSettingsConf.isShowItems();
        ItemStack itemVisibilityItem = showItems ? itemService.changeDisplayNameAndLore(new ItemStack(Material.GLASS), "§7Item Visibility: §cSHOW") : itemService.changeDisplayNameAndLore(new ItemStack(Material.BARRIER), "§7Item Visibility: §cHIDE");

        itemService.populateInventoryLine(inv, 1, glassPane, glassPane, glassPane, renderSystemRepresentationItem, glassPane, itemVisibilityItem, glassPane, glassPane, glassPane);

        return inv;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getInventory() != null && inventories.contains(e.getInventory()) && e.getWhoClicked() instanceof Player) {
            if (itemService.isItemGlassOrBarrier(e.getCurrentItem())) {
                e.setCancelled(true);
                return;
            }

            Player p = (Player) e.getWhoClicked();
            PlayerSettingsConf playerSettingsConf = playerSettingsService.getOrCreateSettingsConf(p);

            e.setCancelled(true);

            if (e.getRawSlot() == 2) {
                // decrease render distance
                int before = playerSettingsConf.getRenderDistance();
                int after = before - 1;
                if (after >= 1) {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    playerSettingsConf.setRenderDistance(after);
                    openInv(p);
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
                }
            } else if (e.getRawSlot() == 6) {
                // increase render distance
                int before = playerSettingsConf.getRenderDistance();
                int after = before + 1;
                if (after <= 64) {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    playerSettingsConf.setRenderDistance(after);
                    openInv(p);
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);

                }
            } else if (e.getRawSlot() == 12) {
                // change render system
                String oldRenderSystemName = playerSettingsConf.getRenderSystemName();
                String newRenderSystemName = null;
                if (oldRenderSystemName.equalsIgnoreCase(VanillaRenderSystem.getDisplayName())) {
                    newRenderSystemName = ModelledRenderSystem.getDisplayName();
                } else if (oldRenderSystemName.equalsIgnoreCase(ModelledRenderSystem.getDisplayName())) {
                    newRenderSystemName = VanillaRenderSystem.getDisplayName();
                }
                playerSettingsConf.setRenderSystemName(newRenderSystemName);

                for (BaseDuctType baseDuctType : ductRegister.baseDuctTypes()) {
                    RenderSystem oldRenderSystem = RenderSystem.getRenderSystem(oldRenderSystemName, baseDuctType);

                    // switch render system
                    synchronized (globalDuctManager.getPlayerDucts(p)) {
                        Iterator<Duct> ductIt = globalDuctManager.getPlayerDucts(p).iterator();
                        while (ductIt.hasNext()) {
                            Duct nextDuct = ductIt.next();
                            protocolService.removeASD(p, oldRenderSystem.getASDForDuct(nextDuct));
                            ductIt.remove();
                        }
                    }

                }

                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openInv(p);
            } else if (e.getRawSlot() == 14) {
                // change item visibility
                boolean showItems = playerSettingsConf.isShowItems();
                playerSettingsConf.setShowItems(!showItems);

                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                openInv(p);
            }

        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getInventory() != null && inventories.contains(e.getInventory()) && e.getPlayer() instanceof Player) {
            inventories.remove(e.getInventory());
        }
    }

}
