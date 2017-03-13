package de.robotricker.transportpipes.pipes.interfaces;

import java.util.List;

import de.robotricker.transportpipes.protocol.ArmorStandData;

public interface Editable {

	/**
	 * if a pipe should be editable without creating a new pipe, e.g. the iron pipe, the pipe class can implement "Editable"<br>
	 * and override this method to edit the ArmorStands without completely recreating the pipe
	 */
	void editArmorStandDatas(List<ArmorStandData> added, List<ArmorStandData> removed);

}
