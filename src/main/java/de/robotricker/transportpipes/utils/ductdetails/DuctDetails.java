package de.robotricker.transportpipes.utils.ductdetails;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;

/**
 * serialization example: DuctType:PIPE;PipeType:COLORED;PipeColor:GREEN or
 * pipe:colored:green
 */
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
		String ductTypeString = ductType.name().toLowerCase(Locale.ENGLISH);
		return ductTypeString;
	}

	public abstract void deserialize(String serialization);

	public abstract boolean doesItemStackMatchesDuctDetails(ItemStack itemStack);

	public static DuctDetails decodeString(String serialization) throws IllegalArgumentException {
		try {
			String ductTypeName = null;
			if (serialization.contains("DuctType:")) {
				ductTypeName = serialization.split(";")[0].split(":")[1];
			} else {
				ductTypeName = serialization.split(":")[0];
			}
			DuctType ductType = DuctType.valueOf(ductTypeName.toUpperCase(Locale.ENGLISH));

			DuctDetails ductDetails = ductType.getDuctDetailsClass().newInstance();
			ductDetails.deserialize(serialization);
			return ductDetails;

		} catch (Exception e) {
			throw new IllegalArgumentException("'" + serialization + "' does not fit the ductDetails format");
		}
	}

	public static boolean hasStringRightFormat(String serialization) {
		try {
			decodeString(serialization);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
