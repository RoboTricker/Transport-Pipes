package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.filter.FilterMode;
import de.robotricker.transportpipes.duct.pipe.filter.FilterStrictness;
import de.robotricker.transportpipes.duct.pipe.filter.ItemData;
import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.location.TPDirection;

public class GoldenPipeSettingsInventory extends DuctSettingsInventory {

    private Map<GoldenPipe.Color, Integer> scrollValues;

    @Override
    public void create() {
        scrollValues = new HashMap<>();
        inv = Bukkit.createInventory(null, 6 * 9, duct.getDuctType().getFormattedTypeName() + " §rInventory");
    }

    @Override
    public void closeForAllPlayers(TransportPipes transportPipes) {
        save(null);
        super.closeForAllPlayers(transportPipes);
    }

    @Override
    public void populate() {
        GoldenPipe pipe = (GoldenPipe) duct;
        Set<TPDirection> allConns = pipe.getAllConnections();
        for (int line = 0; line < 6; line++) {
            GoldenPipe.Color gpc = GoldenPipe.Color.values()[line];
            int scrollValue = scrollValues.getOrDefault(gpc, 0);

            FilterMode filterMode = pipe.getItemFilter(gpc).getFilterMode();
            FilterStrictness filterStrictness = pipe.getItemFilter(gpc).getFilterStrictness();
            ItemStack wool = itemService.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, gpc.getDyeColor().getWoolData()), gpc.getChatColor().toString() + gpc.getDisplayName() + " §7output direction", "§7Filter mode: §c" + filterMode.getDisplayName(), "§7Filter strictness: §c" + filterStrictness.getDisplayName(), "", "§8Left-click to change Filter mode", "§8Right-click to change Filter strictness");
            ItemStack glassPane = itemService.createGlassItem(gpc.getDyeColor());
            ItemStack barrier = itemService.createBarrierItem();
            ItemStack scrollLeft = itemService.changeDisplayName(itemService.createSkullItemStack("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), "§6<<");
            ItemStack scrollRight = itemService.changeDisplayName(itemService.createSkullItemStack("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), "§6>>");

            inv.setItem(line * 9, wool);

            if (filterMode == FilterMode.BLOCK_ALL) {
                for (int i = 1; i < 9; i++) {
                    inv.setItem(line * 9 + i, barrier);
                }
            } else if (!allConns.contains(gpc.getDirection())) {
                for (int i = 1; i < 9; i++) {
                    inv.setItem(line * 9 + i, glassPane);
                }
            } else {
                inv.setItem(line * 9 + 1, scrollLeft);
                inv.setItem(line * 9 + 8, scrollRight);

                ItemData[] filterItems = pipe.getItemFilter(gpc).getFilterItems();
                int indexWithScrollValue = scrollValue;
                for (int i = 2; i < 8; i++) {
                    if (filterItems[indexWithScrollValue] != null) {
                        inv.setItem(line * 9 + i, filterItems[indexWithScrollValue].toItemStack());
                    } else {
                        inv.setItem(line * 9 + i, null);
                    }
                    indexWithScrollValue++;
                }
            }
        }
    }

    @Override
    protected boolean click(Player p, int rawSlot, ClickType ct) {
        GoldenPipe pipe = (GoldenPipe) duct;

        //clicked wool
        if (rawSlot >= 0 && rawSlot < inv.getSize() && rawSlot % 9 == 0) {

            int line = rawSlot / 9;
            GoldenPipe.Color gpc = GoldenPipe.Color.values()[line];
            if (ct == ClickType.LEFT || ct == ClickType.SHIFT_LEFT) {
                pipe.getItemFilter(gpc).setFilterMode(pipe.getItemFilter(gpc).getFilterMode().next());
            } else if (ct == ClickType.RIGHT || ct == ClickType.SHIFT_RIGHT) {
                pipe.getItemFilter(gpc).setFilterStrictness(pipe.getItemFilter(gpc).getFilterStrictness().next());
            }

            save(p);
            populate();

            return true;
        }

        //clicked scroll left
        if (rawSlot >= 0 && rawSlot < inv.getSize() && rawSlot % 9 == 1) {

            save(p);

            int line = rawSlot / 9;
            GoldenPipe.Color gpc = GoldenPipe.Color.values()[line];
            int scrollValue = scrollValues.getOrDefault(gpc, 0);
            if (scrollValue > 0) {
                scrollValue--;
            }
            scrollValues.put(gpc, scrollValue);

            populate();

            return true;
        }

        //clicked scroll right
        if (rawSlot >= 0 && rawSlot < inv.getSize() && rawSlot % 9 == 8) {

            save(p);

            int line = rawSlot / 9;
            GoldenPipe.Color gpc = GoldenPipe.Color.values()[line];
            int scrollValue = scrollValues.getOrDefault(gpc, 0);
            if (scrollValue < ItemFilter.MAX_ITEMS_PER_ROW - 6) {
                scrollValue++;
            }
            scrollValues.put(gpc, scrollValue);

            populate();

            return true;
        }

        return false;
    }

    @Override
    protected void save(Player p) {

        GoldenPipe pipe = (GoldenPipe) duct;

        line_loop:
        for (int line = 0; line < 6; line++) {
            GoldenPipe.Color gpc = GoldenPipe.Color.values()[line];
            ItemData[] filterItems = pipe.getItemFilter(gpc).getFilterItems();
            int scrollValue = scrollValues.getOrDefault(gpc, 0);
            for (int i = 2; i < 8; i++) {
                ItemStack is = inv.getItem(line * 9 + i);
                //make sure the glass pane and barriers won't be saved
                if (!itemService.isItemGlassOrBarrier(is)) {
                    if (is != null && is.getAmount() > 1) {
                        ItemStack drop = is.clone();
                        drop.setAmount(is.getAmount() - 1);
                        if (p != null) {
                            p.getWorld().dropItem(p.getLocation(), drop);
                        } else {
                            duct.getWorld().dropItem(duct.getBlockLoc().toLocation(duct.getWorld()), drop);
                        }
                        is.setAmount(1);
                    }
                    filterItems[scrollValue + i - 2] = is != null ? new ItemData(is) : null;
                } else {
                    continue line_loop;
                }
            }
        }

    }
}
