package de.robotricker.transportpipes.duct.pipe;

import net.querz.nbt.CompoundTag;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.pipe.extractionpipe.ExtractAmount;
import de.robotricker.transportpipes.duct.pipe.extractionpipe.ExtractCondition;
import de.robotricker.transportpipes.duct.pipe.filter.ItemDistributorService;
import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class ExtractionPipe extends Pipe {

    private TPDirection extractDirection;
    private ExtractCondition extractCondition;
    private ExtractAmount extractAmount;
    private ItemFilter itemFilter;

    public ExtractionPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager, ItemDistributorService itemDistributor) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager, itemDistributor);
        this.extractDirection = null;
        this.extractCondition = ExtractCondition.NEEDS_REDSTONE;
        this.extractAmount = ExtractAmount.EXTRACT_1;
        this.itemFilter = new ItemFilter();
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
            globalDuctManager.updateDuctInRenderSystems(this, true);
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

    public ItemFilter getItemFilter() {
        return itemFilter;
    }

    @Override
    public int[] getBreakParticleData() {
        return new int[] { 5, 0 };
    }

    @Override
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> drop = super.destroyed(transportPipes, ductManager, destroyer);
        drop.addAll(itemFilter.getAsItemStacks());
        return drop;
    }

    @Override
    public void saveToNBTTag(CompoundTag compoundTag, ItemService itemService) {
        super.saveToNBTTag(compoundTag, itemService);
    }

    @Override
    public void loadFromNBTTag(CompoundTag compoundTag, ItemService itemService) {
        super.loadFromNBTTag(compoundTag, itemService);
    }
}
