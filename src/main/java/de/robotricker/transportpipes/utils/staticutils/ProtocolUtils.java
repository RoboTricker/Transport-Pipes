package de.robotricker.transportpipes.utils.staticutils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.robotricker.transportpipes.protocol.ArmorStandData;

public class ProtocolUtils{

	public static int[] convertArmorStandListToEntityIdArray(List<ArmorStandData> ASD) {
		Set<Integer> ids = new HashSet<>();
		if (ASD != null) {
			for (ArmorStandData data : ASD) {
				if (data.getEntityID() != -1) {
					ids.add(data.getEntityID());
				}
			}
		}
		int[] idsArray = new int[ids.size()];
		Iterator<Integer> it = ids.iterator();
		for (int i = 0; it.hasNext(); i++) {
			idsArray[i] = it.next();
		}
		return idsArray;
	}

}
