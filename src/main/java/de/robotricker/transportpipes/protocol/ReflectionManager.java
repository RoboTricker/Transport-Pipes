package de.robotricker.transportpipes.protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReflectionManager {

	private static String packageName = Bukkit.getServer().getClass().getPackage().getName();
	private static String version = packageName.substring(packageName.lastIndexOf(".") + 1);
	private static int protocolVersion = -1;

	public static Class<?> getVector3fClass() {
		try {
			return Class.forName("net.minecraft.server." + version + ".Vector3f");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object createVector3f(float x, float y, float z) {
		try {
			Class<?> vectorClass = getVector3fClass();
			if (vectorClass == null) {
				return null;
			}
			Constructor<?> constructor = vectorClass.getConstructor(float.class, float.class, float.class);
			return constructor.newInstance(x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int convertYaw(float yaw) {
		try {
			Class<?> mathHelperClass = Class.forName("net.minecraft.server." + version + ".MathHelper");
			Method dMethod = mathHelperClass.getDeclaredMethod("d", float.class);
			return (int) dMethod.invoke(null, yaw * 256f / 360f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean isFurnaceFuelItem(ItemStack item) {
		try {
			Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
			Method asNMSCopy = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
			Object nmsItemStackObj = asNMSCopy.invoke(null, item);

			Class<?> tileEntityFurnaceClass = Class.forName("net.minecraft.server." + version + ".TileEntityFurnace");
			Method isFuel = tileEntityFurnaceClass.getDeclaredMethod("isFuel", Class.forName("net.minecraft.server." + version + ".ItemStack"));
			return (boolean) isFuel.invoke(null, nmsItemStackObj);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isFurnaceBurnableItem(ItemStack item) {
		try {
			Class<?> recipesFurnaceClass = Class.forName("net.minecraft.server." + version + ".RecipesFurnace");
			Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
			Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");

			Method instanceMethod = recipesFurnaceClass.getDeclaredMethod("getInstance");
			Method getResult = recipesFurnaceClass.getDeclaredMethod("getResult", nmsItemStackClass);
			Method asNMSCopy = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
			Method asBukkitCopy = craftItemStackClass.getDeclaredMethod("asBukkitCopy", nmsItemStackClass);

			Object recipesFurnaceObj = instanceMethod.invoke(null);
			Object nmsItemStackObj = asNMSCopy.invoke(null, item);
			Object resultObj = getResult.invoke(recipesFurnaceObj, nmsItemStackObj);

			if (resultObj == null) {
				return false;
			}
			ItemStack resultItem = (ItemStack) asBukkitCopy.invoke(null, resultObj);
			if (resultItem.getType() != Material.AIR) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int gatherProtocolVersion() {
		if (protocolVersion != -1) {
			return protocolVersion;
		}
		try {

			Class<?> serverClazz = Class.forName("net.minecraft.server." + version + ".MinecraftServer");
			Method getServer = serverClazz.getDeclaredMethod("getServer");
			Object server = getServer.invoke(null);
			Class<?> pingClazz = Class.forName("net.minecraft.server." + version + ".ServerPing");
			Object ping = null;

			for (Field f : serverClazz.getDeclaredFields()) {
				if ((f.getType() == null) || (!f.getType().getSimpleName().equals("ServerPing")))
					continue;
				f.setAccessible(true);
				ping = f.get(server);
			}

			if (ping != null) {
				Object serverData = null;
				for (Field f : pingClazz.getDeclaredFields()) {
					if ((f.getType() == null) || (!f.getType().getSimpleName().endsWith("ServerData")))
						continue;
					f.setAccessible(true);
					serverData = f.get(ping);
				}

				if (serverData != null) {
					int protocolVersion = -1;
					for (Field f : serverData.getClass().getDeclaredFields()) {
						if ((f.getType() == null) || (f.getType() != Integer.TYPE))
							continue;
						f.setAccessible(true);
						protocolVersion = (Integer) f.get(serverData);
					}

					if (protocolVersion != -1) {
						ReflectionManager.protocolVersion = protocolVersion;
						return protocolVersion;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static ItemStack setItemStackUnbreakable(ItemStack is) {
		try {
			Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
			Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
			Class<?> nbtCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

			Method asNMSCopy = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
			Method asCraftMirror = craftItemStackClass.getDeclaredMethod("asCraftMirror", nmsItemStackClass);
			Method nbtSetBoolean = nbtCompoundClass.getDeclaredMethod("setBoolean", String.class, boolean.class);
			Method nmsItemSetTag = nmsItemStackClass.getDeclaredMethod("setTag", nbtCompoundClass);

			Object compound = nbtCompoundClass.newInstance();
			nbtSetBoolean.setAccessible(true);
			nbtSetBoolean.invoke(compound, "Unbreakable", true);

			Object nmsItemStackObj = asNMSCopy.invoke(null, is);
			nmsItemSetTag.setAccessible(true);
			nmsItemSetTag.invoke(nmsItemStackObj, compound);

			is = (ItemStack) asCraftMirror.invoke(null, nmsItemStackObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

}
