package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
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

		Recipe r = e.getInventory().getRecipe();
		if (r == null || e.getViewers().size() != 1) {
			return;
		}

		Player viewer = (Player) e.getViewers().get(0);

		if (r.getResult() != null) {
			PipeType pt = PipeType.getFromPipeItem(r.getResult());
			if (pt != null) {
				if (!viewer.hasPermission(pt.getCraftPermission())) {
					e.getInventory().setResult(null);
					return;
				}
			} else if (PipeItemUtils.isItemStackWrench(r.getResult())) {
				if (!viewer.hasPermission("transportpipes.craft.wrench")) {
					e.getInventory().setResult(null);
					return;
				}
			}
		}

		// prevent colored pipe crafting if the given pipe is not a colored pipe
		if (PipeType.getFromPipeItem(r.getResult()) != null) {
			boolean prevent = false;
			for (int i = 1; i < 10; i++) {
				ItemStack is = e.getInventory().getItem(i);
				if (is != null && is.getType() == Material.SKULL_ITEM && is.getDurability() == SkullType.PLAYER.ordinal()) {
					prevent |= PipeType.getFromPipeItem(is) == null;
				}
			}
			if (prevent) {
				e.getInventory().setResult(null);
			}
		}
	}

}
