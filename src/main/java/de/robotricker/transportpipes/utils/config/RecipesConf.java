package de.robotricker.transportpipes.utils.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.crafting.TPShapedRecipe;
import de.robotricker.transportpipes.utils.crafting.TPShapelessRecipe;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

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
		saveShapelessRecipeAsDefault("colored.white", 1, "pipe:colored", "351:15");
		saveShapelessRecipeAsDefault("colored.blue", 1, "pipe:colored", "351:4");
		saveShapelessRecipeAsDefault("colored.red", 1, "pipe:colored", "351:1");
		saveShapelessRecipeAsDefault("colored.yellow", 1, "pipe:colored", "351:11");
		saveShapelessRecipeAsDefault("colored.green", 1, "pipe:colored", "351:2");
		saveShapelessRecipeAsDefault("colored.black", 1, "pipe:colored", "351:0");
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
		Object nk = createRecipeKey("pipe-" + pt + (pc != null ? "-" + pc : ""));

		String basePath = "recipe." + pt.name().toLowerCase(Locale.ENGLISH);
		if (pc != null) {
			basePath += "." + pc.name().toLowerCase(Locale.ENGLISH);
		}
		ItemStack resultItem;
		if (pt == PipeType.COLORED) {
			resultItem = DuctItemUtils.getClonedDuctItem(new PipeDetails(pc == null ? PipeColor.WHITE : pc));
		} else {
			resultItem = DuctItemUtils.getClonedDuctItem(new PipeDetails(pt));
		}
		resultItem.setAmount((int) read(basePath + ".amount"));
		if (((String) read(basePath + ".type")).equalsIgnoreCase("shaped")) {
			TPShapedRecipe recipe = nk != null ? new TPShapedRecipe((org.bukkit.NamespacedKey) nk, resultItem) : new TPShapedRecipe(resultItem);
			recipe.shape(((List<String>) read(basePath + ".shape")).toArray(new String[0]));
			Collection<String> subKeys = readSubKeys(basePath + ".ingredients");
			for (String key : subKeys) {
				String itemString = (String) read(basePath + ".ingredients." + key);
				recipe.setIngredient(key.charAt(0), itemString);
				// if (item.getData().getData() == 0) {
				// recipe.setIngredient(key.charAt(0), item.getType(), -1);
				// } else {
				// recipe.setIngredient(key.charAt(0), item.getData());
				// }
			}
			recipe.register();
			return recipe;
		} else if (((String) read(basePath + ".type")).equalsIgnoreCase("shapeless")) {
			TPShapelessRecipe recipe = nk != null ? new TPShapelessRecipe((org.bukkit.NamespacedKey) nk, resultItem) : new TPShapelessRecipe(resultItem);
			List<String> ingredients = (List<String>) read(basePath + ".ingredients");
			for (String itemString : ingredients) {
				recipe.addIngredient(itemString);
				// if (item.getData().getData() == 0) {
				// recipe.addIngredient(item.getType(), -1);
				// } else {
				// recipe.addIngredient(item.getData());
				// }
			}
			recipe.register();
			return recipe;
		}
		return null;
	}

	public Recipe createWrenchRecipe() {
		Object nk = createRecipeKey("wrench");

		String basePath = "recipe.wrench";
		ItemStack resultItem = DuctItemUtils.getWrenchItem().clone();
		if (((String) read(basePath + ".type")).equalsIgnoreCase("shaped")) {
			TPShapedRecipe recipe = nk != null ? new TPShapedRecipe((org.bukkit.NamespacedKey) nk, resultItem) : new TPShapedRecipe(resultItem);
			recipe.shape(((List<String>) read(basePath + ".shape")).toArray(new String[0]));
			Collection<String> subKeys = readSubKeys(basePath + ".ingredients");
			for (String key : subKeys) {
				String itemString = (String) read(basePath + ".ingredients." + key);
				recipe.setIngredient(key.charAt(0), itemString);
				// if (item.getData().getData() == 0) {
				// recipe.setIngredient(key.charAt(0), item.getType(), -1);
				// } else {
				// recipe.setIngredient(key.charAt(0), item.getData());
				// }
			}
			recipe.register();
			return recipe;
		} else if (((String) read(basePath + ".type")).equalsIgnoreCase("shapeless")) {
			TPShapelessRecipe recipe = nk != null ? new TPShapelessRecipe((org.bukkit.NamespacedKey) nk, resultItem) : new TPShapelessRecipe(resultItem);
			List<String> ingredients = (List<String>) read(basePath + ".ingredients");
			for (String itemString : ingredients) {
				recipe.addIngredient(itemString);
				// if (item.getData().getData() == 0) {
				// recipe.addIngredient(item.getType(), -1);
				// } else {
				// recipe.addIngredient(item.getData());
				// }
			}
			recipe.register();
			return recipe;
		}
		return null;
	}

	private Object createRecipeKey(String key) {
		try {
			Class.forName("org.bukkit.NamespacedKey");
		} catch (ClassNotFoundException e) {
			return null;
		}
		return new org.bukkit.NamespacedKey(TransportPipes.instance, key);
	}

}
