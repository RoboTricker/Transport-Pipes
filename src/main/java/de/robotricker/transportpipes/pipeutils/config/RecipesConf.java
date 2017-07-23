package de.robotricker.transportpipes.pipeutils.config;

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
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class RecipesConf extends Conf {

	public RecipesConf() {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "recipes.yml"));
		saveAsDefault("recipe.colored.type", "shaped");
		saveAsDefault("recipe.colored.amount", 3);
		saveAsDefault("recipe.colored.shape", Arrays.asList("sss", "ggg", "sss"));
		saveAsDefault("recipe.colored.ingredients.g", "20:0");
		saveAsDefault("recipe.colored.ingredients.s", "280:0");
		saveAsDefault("recipe.golden.type", "shaped");
		saveAsDefault("recipe.golden.amount", 1);
		saveAsDefault("recipe.golden.shape", Arrays.asList("xix", "igi", "xix"));
		saveAsDefault("recipe.golden.ingredients.g", "20:0");
		saveAsDefault("recipe.golden.ingredients.i", "266:0");
		saveAsDefault("recipe.iron.type", "shaped");
		saveAsDefault("recipe.iron.amount", 1);
		saveAsDefault("recipe.iron.shape", Arrays.asList("xix", "igi", "xix"));
		saveAsDefault("recipe.iron.ingredients.g", "20:0");
		saveAsDefault("recipe.iron.ingredients.i", "265:0");
		saveAsDefault("recipe.ice.type", "shaped");
		saveAsDefault("recipe.ice.amount", 1);
		saveAsDefault("recipe.ice.shape", Arrays.asList("xix", "igi", "xix"));
		saveAsDefault("recipe.ice.ingredients.g", "20:0");
		saveAsDefault("recipe.ice.ingredients.i", "80:0");
		saveAsDefault("recipe.colored.white.type", "shapeless");
		saveAsDefault("recipe.colored.white.amount", 1);
		saveAsDefault("recipe.colored.white.ingredients", Arrays.asList("pipe", "351:15"));
		saveAsDefault("recipe.colored.blue.type", "shapeless");
		saveAsDefault("recipe.colored.blue.amount", 1);
		saveAsDefault("recipe.colored.blue.ingredients", Arrays.asList("pipe", "351:4"));
		saveAsDefault("recipe.colored.red.type", "shapeless");
		saveAsDefault("recipe.colored.red.amount", 1);
		saveAsDefault("recipe.colored.red.ingredients", Arrays.asList("pipe", "351:1"));
		saveAsDefault("recipe.colored.yellow.type", "shapeless");
		saveAsDefault("recipe.colored.yellow.amount", 1);
		saveAsDefault("recipe.colored.yellow.ingredients", Arrays.asList("pipe", "351:11"));
		saveAsDefault("recipe.colored.green.type", "shapeless");
		saveAsDefault("recipe.colored.green.amount", 1);
		saveAsDefault("recipe.colored.green.ingredients", Arrays.asList("pipe", "351:2"));
		saveAsDefault("recipe.colored.black.type", "shapeless");
		saveAsDefault("recipe.colored.black.amount", 1);
		saveAsDefault("recipe.colored.black.ingredients", Arrays.asList("pipe", "351:0"));
		saveAsDefault("recipe.wrench.type", "shaped");
		saveAsDefault("recipe.wrench.shape", Arrays.asList("xix", "igi", "xix"));
		saveAsDefault("recipe.wrench.ingredients.g", "331:0");
		saveAsDefault("recipe.wrench.ingredients.i", "280:0");
		finishDefault();
	}

	public Recipe createPipeRecipe(PipeType pt, PipeColor pc) {
		String basePath = "recipe." + pt.name().toLowerCase();
		if (pc != null) {
			basePath += "." + pc.name().toLowerCase();
		}
		ItemStack resultItem = PipeItemUtils.getPipeItem(pt, pc).clone();
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
					typeData = 0;
				}
				if (typeId != -1 && typeData != -1) {
					recipe.setIngredient(key.charAt(0), new MaterialData(typeId, typeData));
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
					typeData = 0;
				}
				if (typeId != -1 && typeData != -1) {
					recipe.addIngredient(new MaterialData(typeId, typeData));
				}
			}
			return recipe;
		}
		return null;
	}

	public Recipe createWrenchRecipe() {
		String basePath = "recipe.wrench";
		ItemStack resultItem = PipeItemUtils.getWrenchItem().clone();
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
					typeData = 0;
				}
				if (typeId != -1 && typeData != -1) {
					recipe.setIngredient(key.charAt(0), new MaterialData(typeId, typeData));
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
					typeData = 0;
				}
				if (typeId != -1 && typeData != -1) {
					recipe.addIngredient(new MaterialData(typeId, typeData));
				}
			}
			return recipe;
		}
		return null;
	}

}
