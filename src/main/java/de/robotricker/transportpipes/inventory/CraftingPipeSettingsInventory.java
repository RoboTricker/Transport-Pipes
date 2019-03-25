package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.PressurePlate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.filter.ItemData;
import de.robotricker.transportpipes.location.TPDirection;

public class CraftingPipeSettingsInventory extends DuctSettingsInventory {

    @Override
    public void create() {
        inv = Bukkit.createInventory(null, 6 * 9, LangConf.Key.DUCT_INVENTORY_TITLE.get(duct.getDuctType().getFormattedTypeName()));
    }

    @Override
    public void closeForAllPlayers(TransportPipes transportPipes) {
        save(null);
        super.closeForAllPlayers(transportPipes);
    }

    @Override
    public void populate() {
        CraftingPipe pipe = (CraftingPipe) duct;
        TPDirection outputDir = pipe.getOutputDir();
        List<ItemStack> cachedItems = pipe.getCachedItems();

        ItemStack glassPane = itemService.createWildcardItem(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glassPane);
        }

        ItemStack outputDirectionItem = itemService.changeDisplayNameAndLoreConfig(new ItemStack(Material.TRIPWIRE_HOOK), LangConf.Key.DUCT_INVENTORY_CRAFTINGPIPE_OUTPUTDIRECTION.getLines(outputDir != null ? outputDir.getDisplayName() : LangConf.Key.DIRECTIONS_NONE.get()));
        inv.setItem(8, outputDirectionItem);

        for (int col = 0; col < 9; col += 4) {
            for (int row = 1; row < 4; row++) {
                inv.setItem(row * 9 + col, glassPane);
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemData id = pipe.getRecipeItems()[i];
            if (id != null) {
                inv.setItem(9 + 1 + (i / 3) * 9 + i % 3, id.toItemStack().clone());
            } else {
                inv.setItem(9 + 1 + (i / 3) * 9 + i % 3, null);
            }
        }

        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                inv.setItem(9 + 5 + (i / 3) * 9 + i % 3, glassPane);
            }
        }

        for (int i = 0; i < 9; i++) {
            inv.setItem(4 * 9 + i, glassPane);
        }

        ItemStack retrieveItemsItem = itemService.changeDisplayNameAndLoreConfig(itemService.createHeadItem("5ca62fac-d094-4346-8361-e1dfdd970607", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19", null), LangConf.Key.DUCT_INVENTORY_CRAFTINGPIPE_RETRIEVECACHEDITEMS.getLines());
        inv.setItem(4 * 9 + 8, retrieveItemsItem);

        for (int i = 0; i < 9; i++) {
            inv.setItem(5 * 9 + i, cachedItems.size() > i ? cachedItems.get(i) : null);
        }

        updateResultWithDelay();

    }

    private Recipe getCraftingRecipe(ItemStack[] items) {

        int col_min = -1;
        int col_max = 0;
        for (int col = 0; col < 3; col++) {
            boolean item = false;
            for (int row = 0; row < 3; row++) {
                item |= items[row * 3 + col] != null;
            }
            if (item && col_min == -1) {
                col_min = col;
            }
            if (item) {
                col_max = col;
            }
        }
        int row_min = -1;
        int row_max = 0;
        for (int row = 0; row < 3; row++) {
            boolean item = false;
            for (int col = 0; col < 3; col++) {
                item |= items[row * 3 + col] != null;
            }
            if (item && row_min == -1) {
                row_min = row;
            }
            if (item) {
                row_max = row;
            }
        }
        int in_rows = row_min == -1 ? 0 : row_max - row_min + 1;
        int in_cols = col_min == -1 ? 0 : col_max - col_min + 1;

        if (in_rows == 0 || in_cols == 0) {
            return null;
        }

        Map<Character, ItemStack> in_map = new HashMap<>();
        String[] in_shape = new String[in_rows];
        char current_char = 'a';
        for (int row = row_min; row <= row_max; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = col_min; col <= col_max; col++) {
                if (items[row * 3 + col] == null) {
                    sb.append(" ");
                } else {
                    char shape_char = '\0';
                    for (Character tempChar : in_map.keySet()) {
                        if (in_map.get(tempChar).equals(items[row * 3 + col])) {
                            shape_char = tempChar;
                        }
                    }
                    if (shape_char == '\0') {
                        shape_char = current_char++;
                        in_map.put(shape_char, items[row * 3 + col]);
                    }
                    sb.append(shape_char);
                }
            }
            in_shape[row - row_min] = sb.toString();
        }

        Iterator<Recipe> recipeIt = Bukkit.recipeIterator();
        recipe_loop:
        while (recipeIt.hasNext()) {
            Recipe recipe = recipeIt.next();
            if (recipe instanceof ShapedRecipe) {
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;

                String[] shape = shapedRecipe.getShape();
                int rows = shape.length;
                int cols = shape[0].length();

                if (in_rows == rows && in_cols == cols) {
                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {
                            char recipeChar = shape[row].charAt(col);
                            char inChar = in_shape[row].charAt(col);
                            if (shapedRecipe.getChoiceMap().get(recipeChar) == null && in_map.get(inChar) == null) {
                                continue;
                            }
                            if (shapedRecipe.getChoiceMap().get(recipeChar) == null || in_map.get(inChar) == null) {
                                continue recipe_loop;
                            }
                            if(shapedRecipe.getChoiceMap().get(recipeChar).test(in_map.get(inChar))) {
                                continue;
                            }
                            continue recipe_loop;
                        }
                    }
                    return shapedRecipe;
                }
            } else if (recipe instanceof ShapelessRecipe) {
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                List<ItemStack> givenItems = new ArrayList<>(Arrays.asList(items));
                givenItems.removeIf(Objects::isNull);
                for (RecipeChoice ingredientChoice : shapelessRecipe.getChoiceList()) {
                    if (!givenItems.removeIf(ingredientChoice::test)) {
                        continue recipe_loop;
                    }
                }
                if (givenItems.isEmpty()) {
                    return shapelessRecipe;
                }
            }
        }
        return null;
    }

    @Override
    protected boolean click(Player p, int rawSlot, ClickType ct) {
        updateResultWithDelay();

        CraftingPipe pipe = (CraftingPipe) duct;

        // clicked change output direction
        if (rawSlot == 8) {
            save(p);
            pipe.updateOutputDirection(true);
            return true;
        }

        // retrieve cached items
        if (rawSlot == 4 * 9 + 8) {
            List<ItemStack> cachedItems = new ArrayList<>(pipe.getCachedItems());
            pipe.getCachedItems().clear();
            transportPipes.runTaskSync(() -> {
                Map<Integer, ItemStack> overflow = p.getInventory().addItem(cachedItems.toArray(new ItemStack[0]));
                for (ItemStack overflowItem : overflow.values()) {
                    p.getWorld().dropItem(p.getLocation(), overflowItem);
                }
            });

            save(p);
            populate();

            return true;
        }

        // clicked on recipe items
        if (slotInRecipeGrid(rawSlot)) {
            return false;
        }

        return rawSlot < inv.getSize();
    }

    @Override
    protected boolean drag(Player p, Set<Integer> rawSlots, DragType dragType) {
        updateResultWithDelay();
        for (Integer i : rawSlots) {
            if (!(i >= inv.getSize() || slotInRecipeGrid(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean slotInRecipeGrid(int rawSlot) {
        if (rawSlot == 9 + 1 || rawSlot == 9 + 2 || rawSlot == 9 + 3) {
            return true;
        }
        if (rawSlot == 2 * 9 + 1 || rawSlot == 2 * 9 + 2 || rawSlot == 2 * 9 + 3) {
            return true;
        }
        if (rawSlot == 3 * 9 + 1 || rawSlot == 3 * 9 + 2 || rawSlot == 3 * 9 + 3) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean collect_to_cursor(Player p, ItemStack cursor, int rawSlot) {
        return cursor != null;
    }

    private Recipe calculateRecipe() {
        ItemStack[] items = new ItemStack[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (inv.getItem(9 + 1 + row * 9 + col) != null) {
                    items[row * 3 + col] = inv.getItem(9 + 1 + row * 9 + col);
                } else {
                    items[row * 3 + col] = null;
                }
            }
        }
        return getCraftingRecipe(items);
    }

    private void updateResultWithDelay() {
        Bukkit.getScheduler().runTask(transportPipes, () -> {
            Recipe recipe = calculateRecipe();
            inv.setItem(24, recipe != null ? recipe.getResult() : null);
        });
    }

    @Override
    public void save(Player p) {

        CraftingPipe pipe = (CraftingPipe) duct;

        for (int i = 0; i < 9; i++) {
            ItemStack is = inv.getItem(9 + 1 + (i / 3) * 9 + i % 3);
            if (is != null && is.getType() != Material.AIR) {
                if (is.getAmount() > 1) {
                    ItemStack drop = is.clone();
                    drop.setAmount(is.getAmount() - 1);
                    if (p != null) {
                        p.getWorld().dropItem(p.getLocation(), drop);
                    } else {
                        duct.getWorld().dropItem(duct.getBlockLoc().toLocation(duct.getWorld()), drop);
                    }
                    is.setAmount(1);
                }
                pipe.getRecipeItems()[i] = new ItemData(is);
            } else {
                pipe.getRecipeItems()[i] = null;
            }
        }

        pipe.setRecipe(calculateRecipe());

    }
}
