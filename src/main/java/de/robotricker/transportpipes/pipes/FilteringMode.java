package de.robotricker.transportpipes.pipes;

import de.robotricker.transportpipes.pipeutils.config.LocConf;

public enum FilteringMode {
	FILTERBY_TYPE(LocConf.FILTERING_FILTERBY_TYPE),
	FILTERBY_TYPE_DAMAGE(LocConf.FILTERING_FILTERBY_TYPE_DAMAGE),
	FILTERBY_TYPE_NBT(LocConf.FILTERING_FILTERBY_TYPE_NBT),
	FILTERBY_TYPE_DAMAGE_NBT(LocConf.FILTERING_FILTERBY_TYPE_DAMAGE_NBT),
	BLOCK_ALL(LocConf.FILTERING_BLOCKALL),
	INVERT(LocConf.FILTERING_INVERT);

	private String locConfKey;

	private FilteringMode(String locConfKey) {
		this.locConfKey = locConfKey;
	}

	public String getLocConfKey() {
		return locConfKey;
	}

	public int getId() {
		return this.ordinal();
	}

	public static FilteringMode fromId(int id) {
		return FilteringMode.values()[id];
	}

	public FilteringMode getNextMode() {
		if (getId() == FilteringMode.values().length - 1) {
			return fromId(0);
		}
		return fromId(getId() + 1);
	}

}