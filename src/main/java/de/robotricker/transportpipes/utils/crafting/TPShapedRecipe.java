package de.robotricker.transportpipes.utils.crafting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public class TPShapedRecipe extends ShapedRecipe implements Listener {

	private ItemStack result;
	private String[] rows;
	private Map<Character, String> ingredients = new HashMap<>();

	public TPShapedRecipe(org.bukkit.NamespacedKey nk, ItemStack result) {
		super(nk, result);
		this.result = result;
	}

	public TPShapedRecipe(ItemStack result) {
		super(result);
		this.result = result;
	}

	public void register() {
		Bukkit.getPluginManager().registerEvents(this, TransportPipes.instance);
		Bukkit.addRecipe(this);
	}

	@Override
	public TPShapedRecipe shape(String... shape) {
		super.shape(shape);

		this.rows = new String[shape.length];
		for (int i = 0; i < shape.length; i++) {
			this.rows[i] = shape[i];
		}

		return this;
	}

	public TPShapedRecipe setIngredient(char c, String serializedIngredient) {
		ingredients.put(c, serializedIngredient);
		setIngredient(c, InventoryUtils.decodeConfigItemStringToMaterialData(serializedIngredient));
		return this;
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {
		if (e.getRecipe() != null && e.getRecipe() instanceof ShapedRecipe) {
			ShapedRecipe sr = (ShapedRecipe) e.getRecipe();

			for (ItemStack bukkitItem : sr.getIngredientMap().values()) {
				if (bukkitItem == null) {
					continue;
				}
				boolean contained = false;
				for (String tpItemString : ingredients.values()) {
					ItemStack tpItem = InventoryUtils.decodeConfigItemString(tpItemString);
					boolean ignoreData = InventoryUtils.shouldItemDataBeIgnored(tpItemString);
					if (tpItem.getType() == bukkitItem.getType() && (tpItem.getData().getData() == bukkitItem.getData().getData() || ignoreData)) {
						contained = true;
						break;
					}
				}
				if (!contained) {
					return;
				}
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
				for (String tpItemString : ingredients.values()) {
					if (InventoryUtils.doesItemStackMatchesConfigItemString(matrixItem, tpItemString)) {
						contained = true;
						break;
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
