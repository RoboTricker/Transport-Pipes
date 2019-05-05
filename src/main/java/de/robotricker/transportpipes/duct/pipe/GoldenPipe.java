package de.robotricker.transportpipes.duct.pipe;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.pipe.filter.ItemDistributorService;
import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.inventory.GoldenPipeSettingsInventory;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class GoldenPipe extends Pipe {

    private ItemFilter[] itemFilters;

    public GoldenPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager, ItemDistributorService itemDistributor) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager, itemDistributor);
        itemFilters = new ItemFilter[Color.values().length];
        for (int i = 0; i < Color.values().length; i++) {
            itemFilters[i] = new ItemFilter();
        }
    }

    public ItemFilter getItemFilter(Color gpc) {
        return itemFilters[gpc.ordinal()];
    }

    public void setItemFilter(Color gpc, ItemFilter itemFilter) {
        itemFilters[gpc.ordinal()] = itemFilter;
    }

    @Override
    protected Map<TPDirection, Integer> calculateItemDistribution(PipeItem pipeItem, TPDirection movingDir, List<TPDirection> dirs, TransportPipes transportPipes) {
        Map<TPDirection, Integer> absWeights = new HashMap<>();
        for (TPDirection dir : dirs) {
            int amount = getItemFilter(Color.getByDir(dir)).applyFilter(pipeItem.getItem());
            absWeights.put(dir, amount);
        }
        return itemDistributor.splitPipeItem(pipeItem.getItem(), absWeights, this);
    }

    @Override
    public Material getBreakParticleData() {
        return Material.GOLD_BLOCK;
    }

    @Override
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> drop = super.destroyed(transportPipes, ductManager, destroyer);
        for (Color gpc : Color.values()) {
            drop.addAll(getItemFilter(gpc).getAsItemStacks());
        }
        return drop;
    }

    @Override
    public void saveToNBTTag(CompoundTag compoundTag, ItemService itemService) {
        super.saveToNBTTag(compoundTag, itemService);

        ListTag<CompoundTag> itemFiltersTag = new ListTag<>(CompoundTag.class);
        for (Color color : Color.values()) {
            CompoundTag filterCompoundTag = new CompoundTag();
            ItemFilter itemFilter = getItemFilter(color);
            itemFilter.saveToNBTTag(filterCompoundTag, itemService);
            itemFiltersTag.add(filterCompoundTag);
        }
        compoundTag.put("itemFilters", itemFiltersTag);

    }

    @Override
    public void loadFromNBTTag(CompoundTag compoundTag, ItemService itemService) {
        super.loadFromNBTTag(compoundTag, itemService);

        ListTag<CompoundTag> itemFiltersTag = (ListTag<CompoundTag>) compoundTag.getListTag("itemFilters");
        for (Color color : Color.values()) {
            ItemFilter itemFilter = new ItemFilter();
            itemFilter.loadFromNBTTag(itemFiltersTag.get(color.ordinal()), itemService);
            itemFilters[color.ordinal()] = itemFilter;
        }

        settingsInv.populate();
    }

    public enum Color {

        BLUE(Material.BLUE_WOOL, Material.BLUE_STAINED_GLASS_PANE, LangConf.Key.COLORS_BLUE.get(), TPDirection.EAST),
        YELLOW(Material.YELLOW_WOOL, Material.YELLOW_STAINED_GLASS_PANE, LangConf.Key.COLORS_YELLOW.get(), TPDirection.WEST),
        RED(Material.RED_WOOL, Material.RED_STAINED_GLASS_PANE, LangConf.Key.COLORS_RED.get(), TPDirection.SOUTH),
        WHITE(Material.WHITE_WOOL, Material.WHITE_STAINED_GLASS_PANE, LangConf.Key.COLORS_WHITE.get(), TPDirection.NORTH),
        GREEN(Material.LIME_WOOL, Material.LIME_STAINED_GLASS_PANE, LangConf.Key.COLORS_GREEN.get(), TPDirection.UP),
        BLACK(Material.BLACK_WOOL, Material.BLACK_STAINED_GLASS_PANE, LangConf.Key.COLORS_BLACK.get(), TPDirection.DOWN);

        private Material woolMaterial;
        private Material glassPaneMaterial;
        private String displayName;
        private TPDirection direction;

        Color(Material woolMaterial, Material glassPaneMaterial, String displayName, TPDirection direction) {
            this.woolMaterial = woolMaterial;
            this.glassPaneMaterial = glassPaneMaterial;
            this.displayName = displayName;
            this.direction = direction;
        }

        public Material getWoolMaterial() {
            return woolMaterial;
        }

        public Material getGlassPaneMaterial() {
            return glassPaneMaterial;
        }

        public String getDisplayName() {
            return displayName;
        }

        public TPDirection getDirection() {
            return direction;
        }

        public static Color getByDir(TPDirection dir) {
            for (Color c : values()) {
                if (c.direction.equals(dir)) {
                    return c;
                }
            }
            return null;
        }
    }

}
