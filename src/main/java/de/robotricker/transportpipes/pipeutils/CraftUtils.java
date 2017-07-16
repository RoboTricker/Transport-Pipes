package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;

public class CraftUtils implements Listener {

	public static void initRecipes() {

		if (!TransportPipes.instance.generalConf.isCraftingEnabled()) {
			return;
		}

		//Recipes
		ShapedRecipe PIPE_ITEM_COLORED_RECIPE = new ShapedRecipe(PipeItemUtils.getPipeItem(PipeType.COLORED, PipeColor.WHITE));
		PIPE_ITEM_COLORED_RECIPE.shape("AAA", "BBB", "AAA");
		PIPE_ITEM_COLORED_RECIPE.setIngredient('A', new MaterialData(Material.STICK, (byte) 0));
		PIPE_ITEM_COLORED_RECIPE.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(PIPE_ITEM_COLORED_RECIPE);

		ShapedRecipe PIPE_ITEM_GOLDEN_RECIPE = new ShapedRecipe(PipeItemUtils.getPipeItem(PipeType.GOLDEN, null));
		PIPE_ITEM_GOLDEN_RECIPE.shape("XAX", "ABA", "XAX");
		PIPE_ITEM_GOLDEN_RECIPE.setIngredient('A', new MaterialData(Material.GOLD_INGOT, (byte) 0));
		PIPE_ITEM_GOLDEN_RECIPE.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(PIPE_ITEM_GOLDEN_RECIPE);

		ShapedRecipe PIPE_ITEM_IRON_RECIPE = new ShapedRecipe(PipeItemUtils.getPipeItem(PipeType.IRON, null));
		PIPE_ITEM_IRON_RECIPE.shape("XAX", "ABA", "XAX");
		PIPE_ITEM_IRON_RECIPE.setIngredient('A', new MaterialData(Material.IRON_INGOT, (byte) 0));
		PIPE_ITEM_IRON_RECIPE.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(PIPE_ITEM_IRON_RECIPE);

		ShapedRecipe PIPE_ITEM_ICE_RECIPE = new ShapedRecipe(PipeItemUtils.getPipeItem(PipeType.ICE, null));
		PIPE_ITEM_ICE_RECIPE.shape("XAX", "ABA", "XAX");
		PIPE_ITEM_ICE_RECIPE.setIngredient('A', new MaterialData(Material.SNOW_BLOCK, (byte) 0));
		PIPE_ITEM_ICE_RECIPE.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(PIPE_ITEM_ICE_RECIPE);

		ShapedRecipe PIPE_ITEM_WRENCH_RECIPE = new ShapedRecipe(PipeItemUtils.getWrenchItem());
		PIPE_ITEM_WRENCH_RECIPE.shape("XAX", "ABA", "XAX");
		PIPE_ITEM_WRENCH_RECIPE.setIngredient('A', new MaterialData(Material.REDSTONE, (byte) 0));
		PIPE_ITEM_WRENCH_RECIPE.setIngredient('B', new MaterialData(Material.STICK, (byte) 0));
		Bukkit.addRecipe(PIPE_ITEM_WRENCH_RECIPE);

		for (PipeColor pipeColor : PipeColor.values()) {
			ShapelessRecipe recipeShapeless = new ShapelessRecipe(PipeItemUtils.getPipeItem(PipeType.COLORED, pipeColor));
			recipeShapeless.addIngredient(new MaterialData(Material.SKULL_ITEM, (byte) SkullType.PLAYER.ordinal()));
			recipeShapeless.addIngredient(pipeColor.getDyeItem().getData());
			Bukkit.addRecipe(recipeShapeless);
		}
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {

		Recipe r = e.getInventory().getRecipe();
		if (r == null) {
			return;
		}

		//prevent colored pipe crafting if the given pipe is not a colored pipe
		if (PipeType.getFromPipeItem(r.getResult()) != null) {
			boolean realPipeItem = false;
			boolean changeColorRecipe = false;
			for (int i = 1; i < 10; i++) {
				ItemStack is = e.getInventory().getItem(i);
				if (is != null && is.getType() == Material.INK_SACK) {
					changeColorRecipe = true;
				}
				if (PipeType.getFromPipeItem(is) == PipeType.COLORED) {
					realPipeItem = true;
				}
			}
			if (!realPipeItem && changeColorRecipe) {
				e.getInventory().setResult(null);
			}
		}
	}

}
