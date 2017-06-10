package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import de.robotricker.transportpipes.TransportPipes;

public class CraftUtils implements Listener {

	public static void initRecipes() {
		//Recipes
		ItemStack result = TransportPipes.PIPE_ITEM.clone();
		result.setAmount(2);
		ShapedRecipe recipe = new ShapedRecipe(result);
		recipe.shape("AAA", "BBB", "AAA");
		recipe.setIngredient('A', new MaterialData(Material.STICK, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(recipe);

		result = TransportPipes.GOLDEN_PIPE_ITEM.clone();
		result.setAmount(1);
		recipe = new ShapedRecipe(result);
		recipe.shape("XAX", "ABA", "XAX");
		recipe.setIngredient('A', new MaterialData(Material.GOLD_INGOT, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(recipe);

		result = TransportPipes.IRON_PIPE_ITEM.clone();
		result.setAmount(2);
		recipe = new ShapedRecipe(result);
		recipe.shape("XAX", "ABA", "XAX");
		recipe.setIngredient('A', new MaterialData(Material.IRON_INGOT, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(recipe);

		result = TransportPipes.WRENCH_ITEM.clone();
		result.setAmount(1);
		recipe = new ShapedRecipe(result);
		recipe.shape("XAX", "ABA", "XAX");
		recipe.setIngredient('A', new MaterialData(Material.REDSTONE, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.STICK, (byte) 0));
		Bukkit.addRecipe(recipe);

		for (PipeColor pipeColor : PipeColor.values()) {
			ShapelessRecipe recipeShapeless = new ShapelessRecipe(TransportPipes.instance.getPipeItem(pipeColor));
			recipeShapeless.addIngredient(Material.BLAZE_ROD);
			recipeShapeless.addIngredient(pipeColor.getDyeItem().getData());
			Bukkit.addRecipe(recipeShapeless);
		}
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {

		if (e.getInventory().getRecipe() == null) {
			return;
		}

		if (e.getInventory().getRecipe().getResult().getType() == Material.BLAZE_POWDER || e.getInventory().getRecipe().getResult().getType() == Material.BREWING_STAND_ITEM) {
			for (int i = 1; i < 10; i++) {
				ItemStack is = e.getInventory().getItem(i);
				if (isPipeItemStack(is)) {
					e.getInventory().setResult(null);
					break;
				}
			}
		} else if (isPipeItemStack(e.getInventory().getRecipe().getResult())) {
			boolean realPipeItem = false;
			boolean changeColorRecipe = false;
			for (int i = 1; i < 10; i++) {
				ItemStack is = e.getInventory().getItem(i);
				if (is != null && is.getType() == Material.INK_SACK) {
					changeColorRecipe = true;
				}
				if (isPipeItemStack(is)) {
					realPipeItem = true;
				}
			}
			if (!realPipeItem && changeColorRecipe) {
				e.getInventory().setResult(null);
			}
		}
	}

	private static boolean isPipeItemStack(ItemStack is) {
		if (is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
			String d = is.getItemMeta().getDisplayName();
			if (d.startsWith("§") && d.endsWith(TransportPipes.instance.PIPE_NAME)) {
				return true;
			} else if (d.equals(TransportPipes.instance.GOLDEN_PIPE_NAME)) {
				return true;
			} else if (d.equals(TransportPipes.instance.IRON_PIPE_NAME)) {
				return true;
			}
		}
		return false;
	}

}
