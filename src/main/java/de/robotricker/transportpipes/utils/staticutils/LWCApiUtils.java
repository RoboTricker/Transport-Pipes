package de.robotricker.transportpipes.utils.staticutils;

import java.util.Map;

import org.bukkit.Location;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;

public class LWCApiUtils extends JavaModule {
	@Override
	public void onPostRegistration(com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent e) {
		//make sure public chests aren't locked from pipes
		if(e.getProtection().getType() == Protection.Type.PUBLIC) {
			return;
		}
		boolean destroyedAtLeastOneDuct = false;

		Location protectionLoc = e.getProtection().getBlock().getLocation();
		BlockLoc protectionBl = BlockLoc.convertBlockLoc(protectionLoc);
		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(protectionLoc.getWorld());
		if (containerMap != null && containerMap.containsKey(protectionBl)) {
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(e.getProtection().getBukkitWorld());
			if (ductMap != null) {
				for (WrappedDirection dir : WrappedDirection.values()) {
					BlockLoc ductLoc = BlockLoc.convertBlockLoc(protectionLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
					if (ductMap.containsKey(ductLoc)) {
						Duct duct = ductMap.get(ductLoc);
						if (duct.getDuctType() == DuctType.PIPE) {
							DuctUtils.destroyDuct(null, duct, true);
							destroyedAtLeastOneDuct = true;
						}
					}
				}
			}
		}

		if (destroyedAtLeastOneDuct) {
			e.getPlayer().sendMessage(LocConf.load(LocConf.LWC_ERROR));
		}
	}
}
