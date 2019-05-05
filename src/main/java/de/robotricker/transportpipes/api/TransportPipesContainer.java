package de.robotricker.transportpipes.api;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.location.TPDirection;

public interface TransportPipesContainer {

    /**
     * should give back the extracted item with the given amount if possible. If there are not enough items left to extract, just give back whats there
     */
    ItemStack extractItem(TPDirection extractDirection, int amount, ItemFilter itemFilter);

    /**
     * should insert the given item into this container block and gives back the item with the amount that did not fit inside or null if all fit inside.
     */
    ItemStack insertItem(TPDirection insertDirection, ItemStack insertion);

    /**
     * does not insert the given item but instead gives back the amount of items of the given type that would fit in this container at the current state.
     * This method is called asynchronously
     */
    int spaceForItem(TPDirection insertDirection, ItemStack insertion);

    boolean isInLoadedChunk();

}
