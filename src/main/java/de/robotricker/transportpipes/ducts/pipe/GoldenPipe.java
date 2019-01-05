package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.manager.DuctManager;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.pipe.goldenpipe.FilterMode;
import de.robotricker.transportpipes.ducts.pipe.goldenpipe.FilterStrictness;
import de.robotricker.transportpipes.ducts.pipe.goldenpipe.ItemData;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.inventory.GoldenPipeSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class GoldenPipe extends Pipe {

    // 1st dimension: output directions in order of GoldenPipe.Color.values()
    // 2nd dimension: filterItems in the specific output direction
    private ItemData[][] filterItems;

    // filterModes in the output directions in order of GoldenPipe.values()
    private FilterMode[] filterModes;

    // filterStrictnesses in the output directions in order of GoldenPipe.values()
    private FilterStrictness[] filterStrictnesses;

    public GoldenPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager);
        filterItems = new ItemData[Color.values().length][GoldenPipeSettingsInventory.MAX_ITEMS_PER_ROW];
        filterModes = new FilterMode[Color.values().length];
        filterStrictnesses = new FilterStrictness[Color.values().length];
        for (int i = 0; i < Color.values().length; i++) {
            filterModes[i] = FilterMode.NORMAL;
            filterStrictnesses[i] = FilterStrictness.TYPE_DAMAGE_METADATA;
        }
    }

    public ItemData[] getFilterItems(Color gpc) {
        return filterItems[gpc.ordinal()];
    }

    public FilterMode getFilterMode(Color gpc) {
        return filterModes[gpc.ordinal()];
    }

    public void setFilterMode(Color gpc, FilterMode filterMode) {
        filterModes[gpc.ordinal()] = filterMode;
    }

    public FilterStrictness getFilterStrictness(Color gpc) {
        return filterStrictnesses[gpc.ordinal()];
    }

    public void setFilterStrictness(Color gpc, FilterStrictness filterStrictness) {
        filterStrictnesses[gpc.ordinal()] = filterStrictness;
    }

    @Override
    public int[] getBreakParticleData() {
        return new int[]{41, 0};
    }

    @Override
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> drop = super.destroyed(transportPipes, ductManager, destroyer);
        for (Color gpc : Color.values()) {
            for (int i = 0; i < GoldenPipeSettingsInventory.MAX_ITEMS_PER_ROW; i++) {
                ItemData itemData = getFilterItems(gpc)[i];
                if (itemData != null) {
                    drop.add(itemData.toItemStack());
                }
            }
        }
        return drop;
    }

    public enum Color {

        BLUE(DyeColor.BLUE, ChatColor.BLUE, "Blue", TPDirection.EAST),
        YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, "Yellow", TPDirection.WEST),
        RED(DyeColor.RED, ChatColor.RED, "Red", TPDirection.SOUTH),
        WHITE(DyeColor.WHITE, ChatColor.WHITE, "White", TPDirection.NORTH),
        GREEN(DyeColor.LIME, ChatColor.GREEN, "Green", TPDirection.UP),
        BLACK(DyeColor.BLACK, ChatColor.BLACK, "Black", TPDirection.DOWN);

        private DyeColor dyeColor;
        private ChatColor chatColor;
        private String displayName;
        private TPDirection direction;

        Color(DyeColor dyeColor, ChatColor chatColor, String displayName, TPDirection direction) {
            this.dyeColor = dyeColor;
            this.chatColor = chatColor;
            this.displayName = displayName;
            this.direction = direction;
        }

        public DyeColor getDyeColor() {
            return dyeColor;
        }

        public ChatColor getChatColor() {
            return chatColor;
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
