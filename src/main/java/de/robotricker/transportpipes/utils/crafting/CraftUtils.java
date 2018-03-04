package de.robotricker.transportpipes.utils.crafting;

import org.bukkit.event.Listener;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;

public class CraftUtils implements Listener {

	public static void initRecipes() {
		if (!TransportPipes.instance.generalConf.isCraftingEnabled()) {
			return;
		}
		for (PipeType pt : PipeType.values()) {
			TransportPipes.instance.recipesConf.createPipeRecipe(pt, null);
			if (pt == PipeType.COLORED) {
				for (PipeColor pc : PipeColor.values()) {
					TransportPipes.instance.recipesConf.createPipeRecipe(pt, pc);
				}
			}
		}

		TransportPipes.instance.recipesConf.createWrenchRecipe();
	}

}
