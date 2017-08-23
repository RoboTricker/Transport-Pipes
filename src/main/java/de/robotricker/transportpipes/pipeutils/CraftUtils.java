package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;

public class CraftUtils implements Listener {

	public static void initRecipes() {
		if (!TransportPipes.instance.generalConf.isCraftingEnabled()) {
			return;
		}
		for (PipeType pt : PipeType.values()) {
			Bukkit.addRecipe(TransportPipes.instance.recipesConf.createPipeRecipe(pt, null));
			if (pt == PipeType.COLORED) {
				for (PipeColor pc : PipeColor.values()) {
					Bukkit.addRecipe(TransportPipes.instance.recipesConf.createPipeRecipe(pt, pc));
				}
			}
		}
		
		Bukkit.addRecipe(TransportPipes.instance.recipesConf.createWrenchRecipe());
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {
		// Prevent more viewers
		if(e.getViewers().size() != 1) {
			return;
		}
		HumanEntity viewer = (HumanEntity) e.getViewers().toArray()[0];

		// Check if a recipe is being used
		Recipe r = e.getInventory().getRecipe();
		if (r == null) {
			return;
		}

		PipeType resultType = PipeType.getFromPipeItem(r.getResult());
		PipeColor resultColor = PipeColor.getPipeColorByPipeItem(r.getResult());

		// If no pipe is being crafted ignore the event
		if(resultType == null) {
			return;
		}

		boolean prevent = false;

		// Check normal pipe permission
		if(resultColor != null && resultColor.equals(PipeColor.WHITE) && !viewer.hasPermission(TransportPipes.instance.generalConf.getPermissionCraftPipe())) {
			prevent = true;
		}
		// Check other pipe permission
		else if(!viewer.hasPermission(resultType.getCraftingPermission())) {
			prevent = true;
		}
		// Prevent colored pipe crafting if the given pipe is not a colored pipe
		else {
			for (int i = 1; i < 10; i++) {
				ItemStack is = e.getInventory().getItem(i);
				if (is != null && is.getType() == Material.SKULL_ITEM && is.getDurability() == SkullType.PLAYER.ordinal()) {
					prevent |= PipeType.getFromPipeItem(is) == null;
				}
			}
		}

		// Cancel result if required
		if (prevent) {
			e.getInventory().setResult(null);
		}
	}

}
