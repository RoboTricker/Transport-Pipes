package de.robotricker.transportpipes.pipes;

import de.robotricker.transportpipes.pipeutils.config.LocConf;

public enum PipeType {

	COLORED(0, "", LocConf.PIPES_COLORED, "transportpipes.craft.coloredpipe"),
	GOLDEN(1, "§6", LocConf.PIPES_GOLDEN, "transportpipes.craft.goldenpipe"),
	IRON(2, "§7", LocConf.PIPES_IRON, "transportpipes.craft.ironpipe"),
	ICE(3, "§b", LocConf.PIPES_ICE, "transportpipes.craft.icepipe"),
	VOID(4, "§5", LocConf.PIPES_VOID, "transportpipes.craft.voidpipe"),
	EXTRACTION(5, "§d", LocConf.PIPES_EXTRACTION, "transportpipes.craft.extractionpipe");

	private int id;
	private String pipeName_colorCode;
	private String pipeName_locConfKey;
	private String craft_permission;

	PipeType(int id, String pipeName_colorCode, String pipeName_locConfKey, String craft_permission) {
		this.id = id;
		this.pipeName_colorCode = pipeName_colorCode;
		this.pipeName_locConfKey = pipeName_locConfKey;
		this.craft_permission = craft_permission;
	}

	public int getId() {
		return id;
	}

	public String getFormattedPipeName() {
		return pipeName_colorCode + LocConf.load(pipeName_locConfKey);
	}
	
	public String getCraftPermission() {
		return craft_permission;
	}

	public static PipeType getFromId(int id) {
		for (PipeType pt : PipeType.values()) {
			if (pt.getId() == id) {
				return pt;
			}
		}
		return null;
	}

}
