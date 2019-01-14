package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.ducts.manager.DuctManager;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.pipe.extractionpipe.ExtractAmount;
import de.robotricker.transportpipes.ducts.pipe.extractionpipe.ExtractCondition;
import de.robotricker.transportpipes.ducts.pipe.goldenpipe.FilterMode;
import de.robotricker.transportpipes.ducts.pipe.goldenpipe.FilterStrictness;
import de.robotricker.transportpipes.ducts.pipe.goldenpipe.ItemData;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.inventory.ExtractionPipeSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class ExtractionPipe extends Pipe {

    private TPDirection extractDirection;
    private ExtractCondition extractCondition;
    private ExtractAmount extractAmount;
    private FilterMode filterMode;
    private FilterStrictness filterStrictness;
    private ItemData[] filterItems;

    public ExtractionPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager);

        this.extractDirection = null;
        this.extractCondition = ExtractCondition.NEEDS_REDSTONE;
        this.extractAmount = ExtractAmount.EXTRACT_1;
        this.filterMode = FilterMode.NORMAL;
        this.filterStrictness = FilterStrictness.TYPE_DAMAGE_METADATA;
        this.filterItems = new ItemData[ExtractionPipeSettingsInventory.MAX_ITEMS_PER_ROW];
    }

    public void updateExtractDirection(boolean cycle) {
        TPDirection oldExtractDirection = getExtractDirection();
        Map<TPDirection, TPContainer> containerConnections = getContainerConnections();
        if (containerConnections.isEmpty()) {
            extractDirection = null;
        } else if (cycle || extractDirection == null || !containerConnections.containsKey(extractDirection)) {
            do {
                if (extractDirection == null) {
                    extractDirection = TPDirection.NORTH;
                } else {
                    extractDirection = extractDirection.next();
                }
            } while (!containerConnections.containsKey(extractDirection));
        }
        if (oldExtractDirection != getExtractDirection()) {
            globalDuctManager.updateDuct(this);
        }
    }

    public TPDirection getExtractDirection() {
        return extractDirection;
    }

    public void setExtractDirection(TPDirection extractDirection) {
        this.extractDirection = extractDirection;
    }

    public ExtractCondition getExtractCondition() {
        return extractCondition;
    }

    public void setExtractCondition(ExtractCondition extractCondition) {
        this.extractCondition = extractCondition;
    }

    public ExtractAmount getExtractAmount() {
        return extractAmount;
    }

    public void setExtractAmount(ExtractAmount extractAmount) {
        this.extractAmount = extractAmount;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public FilterStrictness getFilterStrictness() {
        return filterStrictness;
    }

    public void setFilterStrictness(FilterStrictness filterStrictness) {
        this.filterStrictness = filterStrictness;
    }

    public ItemData[] getFilterItems() {
        return filterItems;
    }

    @Override
    public int[] getBreakParticleData() {
        return new int[] { 5, 0 };
    }

    @Override
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> drop = super.destroyed(transportPipes, ductManager, destroyer);
        for (int i = 0; i < ExtractionPipeSettingsInventory.MAX_ITEMS_PER_ROW; i++) {
            ItemData itemData = getFilterItems()[i];
            if (itemData != null) {
                drop.add(itemData.toItemStack());
            }
        }
        return drop;
    }
}
