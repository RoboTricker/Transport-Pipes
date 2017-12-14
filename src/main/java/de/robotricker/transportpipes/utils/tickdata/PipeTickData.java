package de.robotricker.transportpipes.utils.tickdata;

import java.util.List;

import de.robotricker.transportpipes.pipeitems.PipeItem;

public class PipeTickData extends TickData {

	public boolean extractItems;
	public List<PipeItem> itemsTicked;

	public PipeTickData(boolean extractItems, List<PipeItem> itemsTicked) {
		this.extractItems = extractItems;
		this.itemsTicked = itemsTicked;
	}

}
