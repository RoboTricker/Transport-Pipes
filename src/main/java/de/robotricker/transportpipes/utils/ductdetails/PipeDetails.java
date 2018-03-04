package de.robotricker.transportpipes.utils.ductdetails;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.ColoredPipe;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.IcePipe;
import de.robotricker.transportpipes.duct.pipe.IronPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.VoidPipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import io.sentry.Sentry;

public class PipeDetails extends DuctDetails {

	private PipeType pipeType;
	private PipeColor pipeColor;

	/**
	 * creates new PipeDetails with PipeType pipeType (use other constructor for
	 * COLORED pipeType)
	 * 
	 * if pipeType == null then this pipeDetails object refers to all pipeTypes.
	 * Same concept with pipeColor.
	 */
	public PipeDetails(PipeType pipeType) {
		super(DuctType.PIPE, pipeType.getCraftPermission());
		this.pipeType = pipeType;
	}

	public PipeDetails() {
		super(DuctType.PIPE, null);
	}

	/**
	 * automatically sets PipeType to COLORED
	 */
	public PipeDetails(PipeColor pipeColor) {
		super(DuctType.PIPE, PipeType.COLORED.getCraftPermission());
		this.pipeType = PipeType.COLORED;
		this.pipeColor = pipeColor;
	}

	public PipeType getPipeType() {
		return pipeType;
	}

	public PipeColor getPipeColor() {
		return pipeColor;
	}

	public void setPipeType(PipeType pipeType) {
		this.pipeType = pipeType;
		if (pipeType != null) {
			setCraftPermission(pipeType.getCraftPermission());
		}
	}

	public void setPipeColor(PipeColor pipeColor) {
		this.pipeColor = pipeColor;
	}

	@Override
	public Duct createDuct(Location blockLoc) {
		if (getPipeType() == PipeType.COLORED) {
			return new ColoredPipe(blockLoc, getPipeColor());
		} else if (getPipeType() == PipeType.GOLDEN) {
			return new GoldenPipe(blockLoc);
		} else if (getPipeType() == PipeType.IRON) {
			return new IronPipe(blockLoc);
		} else if (getPipeType() == PipeType.ICE) {
			return new IcePipe(blockLoc);
		} else if (getPipeType() == PipeType.VOID) {
			return new VoidPipe(blockLoc);
		} else if (getPipeType() == PipeType.EXTRACTION) {
			return new ExtractionPipe(blockLoc);
		} else if (getPipeType() == PipeType.CRAFTING) {
			return new CraftingPipe(blockLoc);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ductType == null) ? 0 : ductType.hashCode());
		result = prime * result + ((pipeColor == null) ? 0 : pipeColor.hashCode());
		result = prime * result + ((pipeType == null) ? 0 : pipeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PipeDetails other = (PipeDetails) obj;
		if (pipeType == null || other.pipeType == null)
			return true;
		if (pipeType != other.pipeType)
			return false;
		if (pipeType == PipeType.COLORED && pipeColor != null && other.pipeColor != null && pipeColor != other.pipeColor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String pipeTypeString = pipeType != null ? ":" + pipeType.name().toLowerCase(Locale.ENGLISH) : "";
		String pipeColorString = pipeColor != null ? ":" + pipeColor.name().toLowerCase(Locale.ENGLISH) : "";
		return super.toString() + pipeTypeString + pipeColorString;
	}

	@Override
	public void deserialize(String serialization) throws IllegalArgumentException {
		try {
			String pipeTypeName = null;
			if (serialization.contains("PipeType:")) {
				pipeTypeName = serialization.split(";")[1].split(":")[1];
			} else if (serialization.split(":").length >= 2) {
				pipeTypeName = serialization.split(":")[1];
			}
			setPipeType(pipeTypeName != null ? PipeType.valueOf(pipeTypeName.toUpperCase(Locale.ENGLISH)) : null);
			if (getPipeType() == PipeType.COLORED) {
				String pipeColorName = null;
				if (serialization.contains("PipeColor:")) {
					pipeColorName = serialization.split(";")[2].split(":")[1];
				} else if (serialization.split(":").length >= 3) {
					pipeColorName = serialization.split(":")[2];
				}
				setPipeColor(pipeColorName != null ? PipeColor.valueOf(pipeColorName.toUpperCase(Locale.ENGLISH)) : null);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(serialization + " does not fit the serialization format");
		}
	}

	@Override
	public boolean doesItemStackMatchesDuctDetails(ItemStack itemStack) {
		DuctDetails dd = DuctItemUtils.getDuctDetailsOfItem(itemStack);
		if (dd != null && ductType == dd.ductType) {
			PipeDetails pd = (PipeDetails) dd;
			if (pipeType == null) {
				return true;
			} else if (pipeColor == null) {
				return pipeType == pd.pipeType;
			} else {
				return pipeType == pd.pipeType && pipeColor == pd.pipeColor;
			}
		}
		return false;
	}

}
