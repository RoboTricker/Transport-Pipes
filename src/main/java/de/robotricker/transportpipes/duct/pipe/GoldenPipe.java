package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.pipe.filter.ItemDistributorService;
import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
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

    @Override
    protected Map<TPDirection, Integer> calculateItemDistribution(PipeItem pipeItem, TPDirection movingDir, List<TPDirection> dirs) {
        Map<TPDirection, Integer> absWeights = new HashMap<>();
        for (TPDirection dir : dirs) {
            int amount = getItemFilter(Color.getByDir(dir)).applyFilter(pipeItem.getItem());
            absWeights.put(dir, amount);
        }
        return itemDistributor.splitPipeItem(pipeItem.getItem(), absWeights, this);
    }

    @Override
    public int[] getBreakParticleData() {
        return new int[]{41, 0};
    }

    @Override
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> drop = super.destroyed(transportPipes, ductManager, destroyer);
        for (Color gpc : Color.values()) {
            drop.addAll(getItemFilter(gpc).getAsItemStacks());
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
