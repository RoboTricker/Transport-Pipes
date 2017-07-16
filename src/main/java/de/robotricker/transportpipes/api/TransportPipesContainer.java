package de.robotricker.transportpipes.api;

import de.robotricker.transportpipes.pipeitems.ItemData;
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
	public ItemData extractItem(PipeDirection extractDirection);

	/**
	 * called by TransportPipes if a pipe wants to insert an item into a container block.<br>
	 * <b>Important:</b> The pipe system inserts only 1 item at a time.
	 * 
	 * @param insertDirection
	 *            The direction of the pipe relative to this container block
	 * @param insertion
	 *            the item which will be inserted into this container block
	 * @return whether the item could be inserted (true) or should be rejected back into the pipe system (false).
	 */
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion);

}
