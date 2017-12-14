package de.robotricker.transportpipes.utils.ductdetails;

import org.bukkit.Location;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;

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

	public void setCraftPermission(String craftPermission) {
		this.craftPermission = craftPermission;
	}

	public abstract Duct createDuct(Location blockLoc);
	
	@Override
	public abstract boolean equals(Object other);
	
	@Override
	public abstract int hashCode();

	@Override
	public String toString() {
		String ductTypeString = "DuctType:" + ductType.name() + ";";
		return ductTypeString;
	}
	
	public abstract void fromString(String serialization);
	
}
