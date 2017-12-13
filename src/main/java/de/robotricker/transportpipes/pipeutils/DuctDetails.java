package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Location;

import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.DuctType;

public abstract class DuctDetails {

	protected DuctType ductType;
	protected String craftPermission;

	public DuctDetails(DuctType ductType, String craftPermission) {
		this.ductType = ductType;
		this.craftPermission = craftPermission;
	}

	public DuctType getDuctType() {
		return ductType;
	}
	
	public String getCraftPermission() {
		return craftPermission;
	}
	
	public abstract Duct createDuct(Location blockLoc);
	
	@Override
	public abstract boolean equals(Object other);
	
	@Override
	public abstract int hashCode();

}
