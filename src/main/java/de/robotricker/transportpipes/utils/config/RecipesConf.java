package de.robotricker.transportpipes.utils.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class RecipesConf extends Conf {

	public RecipesConf() {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "recipes.yml"), TransportPipes.instance);
		saveShapedRecipeAsDefault("colored", 4, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "280:0");
		saveShapedRecipeAsDefault("golden", 1, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "41:0");
		saveShapedRecipeAsDefault("iron", 1, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "42:0");
		saveShapedRecipeAsDefault("ice", 1, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "80:0");
		saveShapedRecipeAsDefault("void", 1, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "49:0");
		saveShapedRecipeAsDefault("extraction", 1, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "5");
		saveShapedRecipeAsDefault("crafting", 1, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "58:0");
		saveShapelessRecipeAsDefault("colored.white", 1, "pipe", "351:15");
		saveShapelessRecipeAsDefault("colored.blue", 1, "pipe", "351:4");
		saveShapelessRecipeAsDefault("colored.red", 1, "pipe", "351:1");
		saveShapelessRecipeAsDefault("colored.yellow", 1, "pipe", "351:11");
		saveShapelessRecipeAsDefault("colored.green", 1, "pipe", "351:2");
		saveShapelessRecipeAsDefault("colored.black", 1, "pipe", "351:0");
		saveShapedRecipeAsDefault("wrench", 1, Arrays.asList("xrx", "rsr", "xrx"), "r", "331:0", "s", "280:0");
		finishDefault();
	}

	private void saveShapedRecipeAsDefault(String name, int amount, List<String> shape, String... ingredients) {
		String fullKey = "recipe." + name;
		if (!getYamlConf().contains(fullKey + ".type")) {
			saveAsDefault(fullKey + ".type", "shaped");
			saveAsDefault(fullKey + ".amount", amount);
			saveAsDefault(fullKey + ".shape", shape);
			for (int i = 0; i < ingredients.length; i++) {
				char ingredientChar = ingredients[i].charAt(0);
				String ingredientValue = ingredients[++i];
				saveAsDefault(fullKey + ".ingredients." + ingredientChar, ingredientValue);
			}
		}
	}

	private void saveShapelessRecipeAsDefault(String name, int amount, String... ingredients) {
		String fullKey = "recipe." + name;
		if (!getYamlConf().contains(fullKey + ".type")) {
			saveAsDefault(fullKey + ".type", "shapeless");
			saveAsDefault(fullKey + ".amount", amount);
			saveAsDefault(fullKey + ".ingredients", Arrays.asList(ingredients));
		}
	}

	/**
	 * prevent unused key from removing only inside RecipesConf
	 */
	@Override
	protected void finishDefault() {
		saveToFile();
	}

	public Recipe createPipeRecipe(PipeType pt, PipeColor pc) {
		String basePath = "recipe." + pt.name().toLowerCase();
		if (pc != null) {
			basePath += "." + pc.name().toLowerCase();
		}
		ItemStack resultItem;
		if (pt == PipeType.COLORED) {
			resultItem = DuctItemUtils.getClonedDuctItem(new PipeDetails(pc == null ? PipeColor.WHITE : pc));
		} else {
			resultItem = DuctItemUtils.getClonedDuctItem(new PipeDetails(pt));
		}
		resultItem.setAmount((int) read(basePath + ".amount"));
		if (((String) read(basePath + ".type")).equalsIgnoreCase("shaped")) {
			ShapedRecipe recipe = new ShapedRecipe(resultItem);
			recipe.shape(((List<String>) read(basePath + ".shape")).toArray(new String[0]));
			Collection<String> subKeys = readSubKeys(basePath + ".ingredients");
			for (String key : subKeys) {
				String itemString = (String) read(basePath + ".ingredients." + key);
				int typeId = -1;
				byte typeData = -1;
				if (itemString.equalsIgnoreCase("pipe")) {
					typeId = Material.SKULL_ITEM.getId();
					typeData = (byte) SkullType.PLAYER.ordinal();
				} else if (itemString.contains(":")) {
					typeId = Integer.parseInt(itemString.split(":")[0]);
					typeData = Byte.parseByte(itemString.split(":")[1]);
				} else {
					typeId = Integer.parseInt(itemString);
				}
				if (typeId != -1 && typeData != -1) {
					recipe.setIngredient(key.charAt(0), new MaterialData(typeId, typeData));
				} else if (typeId != -1) {
					recipe.setIngredient(key.charAt(0), Material.getMaterial(typeId), -1);
				}
			}
			return recipe;
		} else if (((String) read(basePath + ".type")).equalsIgnoreCase("shapeless")) {
			ShapelessRecipe recipe = new ShapelessRecipe(resultItem);
			List<String> ingredients = (List<String>) read(basePath + ".ingredients");
			for (String itemString : ingredients) {
				int typeId = -1;
				byte typeData = -1;
				if (itemString.equalsIgnoreCase("pipe")) {
					typeId = Material.SKULL_ITEM.getId();
					typeData = (byte) SkullType.PLAYER.ordinal();
				} else if (itemString.contains(":")) {
					typeId = Integer.parseInt(itemString.split(":")[0]);
					typeData = Byte.parseByte(itemString.split(":")[1]);
				} else {
					typeId = Integer.parseInt(itemString);
				}
				if (typeId != -1 && typeData != -1) {
					recipe.addIngredient(new MaterialData(typeId, typeData));
				} else if (typeId != -1) {
					recipe.addIngredient(Material.getMaterial(typeId), -1);
				}
			}
			return recipe;
		}
		return null;
	}

	public Recipe createWrenchRecipe() {
		String basePath = "recipe.wrench";
		ItemStack resultItem = DuctItemUtils.getWrenchItem().clone();
		if (((String) read(basePath + ".type")).equalsIgnoreCase("shaped")) {
			ShapedRecipe recipe = new ShapedRecipe(resultItem);
			recipe.shape(((List<String>) read(basePath + ".shape")).toArray(new String[0]));
			Collection<String> subKeys = readSubKeys(basePath + ".ingredients");
			for (String key : subKeys) {
				String itemString = (String) read(basePath + ".ingredients." + key);
				int typeId = -1;
				byte typeData = -1;
				if (itemString.equalsIgnoreCase("pipe")) {
					typeId = Material.SKULL_ITEM.getId();
					typeData = (byte) SkullType.PLAYER.ordinal();
				} else if (itemString.contains(":")) {
					typeId = Integer.parseInt(itemString.split(":")[0]);
					typeData = Byte.parseByte(itemString.split(":")[1]);
				} else {
					typeId = Integer.parseInt(itemString);
				}
				if (typeId != -1 && typeData != -1) {
					recipe.setIngredient(key.charAt(0), new MaterialData(typeId, typeData));
				} else if (typeId != -1) {
					recipe.setIngredient(key.charAt(0), Material.getMaterial(typeId), -1);
				}
			}
			return recipe;
		} else if (((String) read(basePath + ".type")).equalsIgnoreCase("shapeless")) {
			ShapelessRecipe recipe = new ShapelessRecipe(resultItem);
			List<String> ingredients = (List<String>) read(basePath + ".ingredients");
			for (String itemString : ingredients) {
				int typeId = -1;
				byte typeData = -1;
				if (itemString.equalsIgnoreCase("pipe")) {
					typeId = Material.SKULL_ITEM.getId();
					typeData = (byte) SkullType.PLAYER.ordinal();
				} else if (itemString.contains(":")) {
					typeId = Integer.parseInt(itemString.split(":")[0]);
					typeData = Byte.parseByte(itemString.split(":")[1]);
				} else {
					typeId = Integer.parseInt(itemString);
				}
				if (typeId != -1 && typeData != -1) {
					recipe.addIngredient(new MaterialData(typeId, typeData));
				} else if (typeId != -1) {
					recipe.addIngredient(Material.getMaterial(typeId), -1);
				}
			}
			return recipe;
		}
		return null;
	}

}
