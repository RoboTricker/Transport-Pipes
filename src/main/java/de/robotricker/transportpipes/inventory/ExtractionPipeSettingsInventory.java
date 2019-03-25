package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.filter.FilterMode;
import de.robotricker.transportpipes.duct.pipe.filter.ItemData;
import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.location.TPDirection;

public class ExtractionPipeSettingsInventory extends DuctSettingsInventory {

    private int scrollValue;

    public ExtractionPipeSettingsInventory() {
        scrollValue = 0;
    }

    @Override
    public void create() {
        inv = Bukkit.createInventory(null, 3 * 9, LangConf.Key.DUCT_INVENTORY_TITLE.get(duct.getDuctType().getFormattedTypeName()));
    }

    @Override
    public void closeForAllPlayers(TransportPipes transportPipes) {
        save(null);
        super.closeForAllPlayers(transportPipes);
    }

    @Override
    public void populate() {
        ExtractionPipe pipe = (ExtractionPipe) duct;
        TPDirection extractDir = pipe.getExtractDirection();

        ItemStack extractDirection = itemService.changeDisplayNameAndLoreConfig(new ItemStack(Material.TRIPWIRE_HOOK), LangConf.Key.DUCT_INVENTORY_EXTRACTIONPIPE_EXTRACTDIRECTION.getLines(extractDir != null ? extractDir.getDisplayName() : LangConf.Key.DIRECTIONS_NONE.get()));
        ItemStack extractCondition = itemService.changeDisplayNameAndLoreConfig(pipe.getExtractCondition().getDisplayItem(), LangConf.Key.DUCT_INVENTORY_EXTRACTIONPIPE_EXTRACTCONDITION.getLines(pipe.getExtractCondition().getDisplayName()));
        ItemStack extractAmount = itemService.changeDisplayNameAndLoreConfig(pipe.getExtractAmount().getDisplayItem(), LangConf.Key.DUCT_INVENTORY_EXTRACTIONPIPE_EXTRACTAMOUNT.getLines(pipe.getExtractAmount().getDisplayName()));

        ItemStack wool = itemService.changeDisplayNameAndLoreConfig(new ItemStack(Material.WHITE_WOOL), LangConf.Key.DUCT_INVENTORY_EXTRACTIONPIPE_FILTERTITLE.get(), LangConf.Key.DUCT_INVENTORY_FILTER_MODE_AND_STRICTNESS.getLines(pipe.getItemFilter().getFilterMode().getDisplayName(), pipe.getItemFilter().getFilterStrictness().getDisplayName()));
        ItemStack scrollLeft = itemService.changeDisplayName(itemService.createHeadItem("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), LangConf.Key.DUCT_INVENTORY_LEFTARROW.get());
        ItemStack scrollRight = itemService.changeDisplayName(itemService.createHeadItem("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), LangConf.Key.DUCT_INVENTORY_RIGHTARROW.get());

        // basic settings
        for (int i = 0; i < 18; i++) {
            if (i == 2) {
                inv.setItem(i, extractDirection);
            } else if (i == 4) {
                inv.setItem(i, extractAmount);
            } else if (i == 6) {
                inv.setItem(i, extractCondition);
            } else {
                inv.setItem(i, itemService.createWildcardItem(Material.GRAY_STAINED_GLASS_PANE));
            }
        }

        inv.setItem(18, wool);

        // filtering stuff
        if (pipe.getItemFilter().getFilterMode() == FilterMode.BLOCK_ALL) {
            for (int i = 1; i < 9; i++) {
                inv.setItem(18 + i, itemService.createBarrierItem());
            }
        } else {
            inv.setItem(18 + 1, scrollLeft);
            inv.setItem(18 + 8, scrollRight);

            ItemData[] items = pipe.getItemFilter().getFilterItems();
            int indexWithScrollValue = scrollValue;
            for (int i = 2; i < 8; i++) {
                if (items[indexWithScrollValue] != null) {
                    inv.setItem(18 + i, items[indexWithScrollValue].toItemStack());
                } else {
                    inv.setItem(18 + i, null);
                }
                indexWithScrollValue++;
            }
        }
    }

    @Override
    protected boolean click(Player p, int rawSlot, ClickType ct) {
        ExtractionPipe pipe = (ExtractionPipe) duct;

        // clicked change extract direction
        if (rawSlot == 2) {
            save(p);
            pipe.updateExtractDirection(true);
            return true;
        }

        // clicked change extract amount
        if (rawSlot == 4) {
            save(p);
            pipe.setExtractAmount(pipe.getExtractAmount().next());
            populate();
            return true;
        }

        // clicked change extract condition
        if (rawSlot == 6) {
            save(p);
            pipe.setExtractCondition(pipe.getExtractCondition().next());
            populate();
            return true;
        }

        // clicked filtering mode wool
        if (rawSlot == 18) {
            if (ct == ClickType.LEFT || ct == ClickType.SHIFT_LEFT) {
                pipe.getItemFilter().setFilterMode(pipe.getItemFilter().getFilterMode().next());
            } else if (ct == ClickType.RIGHT || ct == ClickType.SHIFT_RIGHT) {
                pipe.getItemFilter().setFilterStrictness(pipe.getItemFilter().getFilterStrictness().next());
            }
            save(p);
            populate();
            return true;
        }

        // clicked scroll left
        if (rawSlot == 19) {
            save(p);
            if (scrollValue > 0) {
                scrollValue--;
            }
            populate();
            return true;
        }

        // clicked scroll right
        if (rawSlot == 26) {
            save(p);
            if (scrollValue < ItemFilter.MAX_ITEMS_PER_ROW - 6) {
                scrollValue++;
            }
            populate();
            return true;
        }

        return false;
    }

    @Override
    public void save(Player p) {
        ExtractionPipe pipe = (ExtractionPipe) duct;

        ItemData[] items = pipe.getItemFilter().getFilterItems();
        for (int i = 2; i < 8; i++) {
            ItemStack itemStack = inv.getItem(18 + i);
            if (itemService.isItemWildcardOrBarrier(itemStack)) {
                return;
            }
            if (itemStack != null && itemStack.getAmount() > 1) {
                ItemStack drop = itemStack.clone();
                drop.setAmount(itemStack.getAmount() - 1);
                p.getWorld().dropItem(p.getLocation(), drop);
                itemStack.setAmount(1);
            }
            items[scrollValue + i - 2] = itemStack != null ? new ItemData(itemStack) : null;
        }

    }
}
