package de.robotricker.transportpipes.api;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.PipeDirection;

public interface TransportPipesContainer {

	/**
	 * called by TransportPipes if a pipe wants to extract an item from a container block.<br>
	 * <b>Important:</b> The pipe system extracts only 1 item at a time.
	 * 
	 * @param extractDirection
	 *            The direction of the pipe relative to this container block.
	 * @return returns an ItemData object of the extracted item.
	 */
	public ItemStack extractItem(PipeDirection extractDirection, int extractAmount);

	/**
	 * called by TransportPipes if a pipe wants to insert an item into a container block.<br>
	 * <b>Important:</b> The pipe system inserts only 1 item at a time.
	 * 
	 * @param insertDirection
	 *            The direction of the pipe relative to this container block
	 * @param insertion
	 *            the item which will be inserted into this container block
	 * @return whether the item can be inserted.
	 */
	public ItemStack insertItem(PipeDirection insertDirection, ItemStack insertion);

	/**
	 * called by TransportPipes if a pipe wants to know if an item could be inserted into a container block<br>
	 * but doesn't insert it yet.<br>
	 * This method is called on the Transport-Pipes Thread. Keep in mind that it's asynchronous!<br>
	 * This method returns the amount of items of type "insertion" that would fit into this container block. It doesn't matter what amount "insertion" has.
	 * 
	 * @param insertDirection
	 *            The direction of the pipe relative to this container block
	 * @param insertion
	 *            the item which will be inserted into this container block
	 * @return the amount of items that would fit into this container block. E.g. an empty chest should return 27 * insertion.getMaxStackSize(). If this amount is zero, none of the items fit.
	 */
	public int howMuchSpaceForItemAsync(PipeDirection insertDirection, ItemStack insertion);

}
