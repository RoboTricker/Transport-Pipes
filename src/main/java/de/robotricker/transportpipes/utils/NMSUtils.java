package de.robotricker.transportpipes.utils;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

public final class NMSUtils {

    private static String packageName = Bukkit.getServer().getClass().getPackage().getName();
    private static String version = packageName.substring(packageName.lastIndexOf(".") + 1);
    private static int protocolVersion = -1;

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

    public static Class<?> getVector3fClass() {
        try {
            return Class.forName("net.minecraft.server." + version + ".Vector3f");
        } catch (ClassNotFoundException e) {
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

    public static int gatherProtocolVersion() {
        if (protocolVersion != -1) {
            return protocolVersion;
        }
        try {

            Class<?> serverClazz = Class.forName("net.minecraft.server." + version + ".MinecraftServer");
            Object server = MethodUtils.invokeExactStaticMethod(serverClazz, "getServer");

            Class<?> pingClazz = Class.forName("net.minecraft.server." + version + ".ServerPing");
            Object ping = null;
            for (Field currentField : serverClazz.getDeclaredFields()) {
                if ((currentField.getType() == null) || (!currentField.getType().getSimpleName().endsWith("ServerPing"))) {
                    continue;
                }
                ping = FieldUtils.readField(currentField, server, true);
            }

            if (ping == null) {
                return -1;
            }

            Object serverData = null;
            for (Field currentField : pingClazz.getDeclaredFields()) {
                if ((currentField.getType() == null) || (!currentField.getType().getSimpleName().endsWith("ServerData"))) {
                    continue;
                }
                serverData = FieldUtils.readField(currentField, ping, true);
            }

            if (serverData == null) {
                return -1;
            }

            int protocolVersion = -1;
            for (Field currentField : serverData.getClass().getDeclaredFields()) {
                if ((currentField.getType() == null) || (currentField.getType() != Integer.TYPE)) {
                    continue;
                }
                protocolVersion = (int) FieldUtils.readField(currentField, serverData, true);
            }

            if (protocolVersion != -1) {
                NMSUtils.protocolVersion = protocolVersion;
                return protocolVersion;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public static ItemStack manipulateItemStackNBT(ItemStack is, String tagName, Object tagValue, Class<?> tagType, String tagTypeName) {
        try {

            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
            Class<?> nbtCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

            Method asNMSCopy = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Method asBukkitCopy = craftItemStackClass.getDeclaredMethod("asBukkitCopy", nmsItemStackClass);
            Method nmsItemHasTag = nmsItemStackClass.getDeclaredMethod("hasTag");
            Method nmsItemGetTag = nmsItemStackClass.getDeclaredMethod("getTag");
            Method nmsItemSetTag = nmsItemStackClass.getDeclaredMethod("setTag", nbtCompoundClass);
            Method nbtSetValue = nbtCompoundClass.getDeclaredMethod("set" + tagTypeName, String.class, tagType);

            Object nmsItemStackObj = asNMSCopy.invoke(null, is);

            nmsItemHasTag.setAccessible(true);
            boolean hasTag = (boolean) nmsItemHasTag.invoke(nmsItemStackObj);

            Object compound;
            if (hasTag) {
                nmsItemGetTag.setAccessible(true);
                compound = nmsItemGetTag.invoke(nmsItemStackObj);
            } else {
                compound = nbtCompoundClass.newInstance();
            }

            nbtSetValue.setAccessible(true);
            nbtSetValue.invoke(compound, tagName, tagValue);

            nmsItemSetTag.setAccessible(true);
            nmsItemSetTag.invoke(nmsItemStackObj, compound);

            is = (ItemStack) asBukkitCopy.invoke(null, nmsItemStackObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    public static Object readItemStackNBT(ItemStack is, String tagName, String tagTypeName) {
        try {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
            Class<?> nbtCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

            Method asNMSCopy = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Method nmsItemHasTag = nmsItemStackClass.getDeclaredMethod("hasTag");
            Method nmsItemGetTag = nmsItemStackClass.getDeclaredMethod("getTag");
            Method nbtGetValue = nbtCompoundClass.getDeclaredMethod("get" + tagTypeName, String.class);

            Object nmsItemStackObj = asNMSCopy.invoke(null, is);

            nmsItemHasTag.setAccessible(true);
            boolean hasTag = (boolean) nmsItemHasTag.invoke(nmsItemStackObj);

            Object compound;
            if (hasTag) {
                nmsItemGetTag.setAccessible(true);
                compound = nmsItemGetTag.invoke(nmsItemStackObj);
            } else {
                compound = nbtCompoundClass.newInstance();
            }

            nbtGetValue.setAccessible(true);
            Object value = nbtGetValue.invoke(compound, tagName);

            return value;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack setItemStackUnbreakable(ItemStack is) {
        return manipulateItemStackNBT(is, "Unbreakable", true, boolean.class, "Boolean");
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

        Iterator<Recipe> recipeIt = Bukkit.recipeIterator();
        while (recipeIt.hasNext()) {
            Recipe recipe = recipeIt.next();
            if (!(recipe instanceof FurnaceRecipe))
                continue;
            if(!((FurnaceRecipe) recipe).getInputChoice().test(item))
                continue;
            return true;
        }

        return false;
    }

}
