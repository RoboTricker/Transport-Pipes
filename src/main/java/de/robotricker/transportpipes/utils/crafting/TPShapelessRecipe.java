package de.robotricker.transportpipes.utils.crafting;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public class TPShapelessRecipe extends ShapelessRecipe implements Listener {

	private ItemStack result;
	private List<String> ingredients = new ArrayList<>();

	public TPShapelessRecipe(org.bukkit.NamespacedKey nk, ItemStack result) {
		super(nk, result);
		this.result = result;
	}

	public TPShapelessRecipe(ItemStack result) {
		super(result);
		this.result = result;
	}

	public void register() {
		Bukkit.getPluginManager().registerEvents(this, TransportPipes.instance);
		Bukkit.addRecipe(this);
	}

	public TPShapelessRecipe addIngredient(String serializedIngredient) {
		ingredients.add(serializedIngredient);
		addIngredient(InventoryUtils.decodeConfigItemStringToMaterialData(serializedIngredient));
		return this;
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {
		if (e.getRecipe() != null && e.getRecipe() instanceof ShapelessRecipe) {
			ShapelessRecipe sr = (ShapelessRecipe) e.getRecipe();
			if (sr.getIngredientList().size() == ingredients.size()) {
				for (int i = 0; i < ingredients.size(); i++) {
					ItemStack bukkitItem = sr.getIngredientList().get(i);
					ItemStack tpItem = InventoryUtils.decodeConfigItemString(ingredients.get(i));
					boolean ignoreData = InventoryUtils.shouldItemDataBeIgnored(ingredients.get(i));
					if (bukkitItem.getType() != tpItem.getType() || (bukkitItem.getData().getData() != tpItem.getData().getData() && !ignoreData)) {
						return;
					}
				}
			} else {
				return;
			}
			if (!result.isSimilar(sr.getResult())) {
				return;
			}

			// until this point, I just made sure that the following code block only gets
			// called if this recipe actually is handled.
			// The following code makes sure that all the metadata of the items are right
			// and sets the result to null if they're not.
			for (int i = 1; i < 10; i++) {
				ItemStack matrixItem = e.getInventory().getItem(i);
				if (matrixItem == null) {
					continue;
				}
				boolean contained = false;
				for (String ingredientString : ingredients) {
					if (InventoryUtils.doesItemStackMatchesConfigItemString(matrixItem, ingredientString)) {
						contained = true;
					}
				}
				if (!contained) {
					e.getInventory().setResult(null);
					return;
				}
			}
		}
	}

}
