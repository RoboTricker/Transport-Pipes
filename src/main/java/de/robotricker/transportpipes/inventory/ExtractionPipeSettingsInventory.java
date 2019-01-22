package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
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
        inv = Bukkit.createInventory(null, 3 * 9, duct.getDuctType().getFormattedTypeName() + " §rInventory");
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

        ItemStack extractDirection = itemService.changeDisplayNameAndLore(new ItemStack(Material.TRIPWIRE_HOOK), extractDir != null ? "§7Extract Direction: §c" + extractDir.name() : "§7Extract Direction: §cNONE", "", "§8Click to change");
        ItemStack extractCondition = itemService.changeDisplayNameAndLore(pipe.getExtractCondition().getDisplayItem(), "§7Extract Condition: §c" + pipe.getExtractCondition().getDisplayName(), "", "§8Click to change");
        ItemStack extractAmount = itemService.changeDisplayNameAndLore(pipe.getExtractAmount().getDisplayItem(), "§7Extract Amount: §c" + pipe.getExtractAmount().getDisplayName(), "", "§8Click to change");

        ItemStack wool = itemService.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 0), "§6Extraction Filter", "§7Filter Mode: §c" + pipe.getItemFilter().getFilterMode().getDisplayName(), "§7Filter Strictness: §c" + pipe.getItemFilter().getFilterStrictness().getDisplayName(), "", "§8Left-click to change Filter mode", "§8Right-click to change Filter strictness");
        ItemStack scrollLeft = itemService.changeDisplayName(itemService.createSkullItemStack("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), "§6<<");
        ItemStack scrollRight = itemService.changeDisplayName(itemService.createSkullItemStack("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), "§6>>");

        // basic settings
        for (int i = 0; i < 18; i++) {
            if (i == 2) {
                inv.setItem(i, extractDirection);
            } else if (i == 4) {
                inv.setItem(i, extractAmount);
            } else if (i == 6) {
                inv.setItem(i, extractCondition);
            } else {
                inv.setItem(i, itemService.createGlassItem(DyeColor.GRAY));
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
            populate();
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
    protected void save(Player p) {
        ExtractionPipe pipe = (ExtractionPipe) duct;

        ItemData[] items = pipe.getItemFilter().getFilterItems();
        for (int i = 2; i < 8; i++) {
            ItemStack itemStack = inv.getItem(18 + i);
            if (itemService.isItemGlassOrBarrier(itemStack)) {
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
