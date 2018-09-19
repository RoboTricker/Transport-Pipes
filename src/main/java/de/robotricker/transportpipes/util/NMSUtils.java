package de.robotricker.transportpipes.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class NMSUtils {

    private static String packageName = Bukkit.getServer().getClass().getPackage().getName();
    private static String version = packageName.substring(packageName.lastIndexOf(".") + 1);
    private static int protocolVersion = -1;

    private NMSUtils() {
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
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getVector3fClass() {
        try {
            return Class.forName("net.minecraft.server." + version + ".Vector3f");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static int convertYaw(float yaw) {
        try {
            Class<?> mathHelperClass = Class.forName("net.minecraft.server." + version + ".MathHelper");
            Method dMethod = mathHelperClass.getDeclaredMethod("d", float.class);
            return (int) dMethod.invoke(null, yaw * 256f / 360f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: move into a proper class with version enum
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

    public static ItemStack manipulateItemStackNBT(ItemStack item, String tagName, Object tagValue, Class<?> tagType, String tagTypeName) {
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

            Object nmsItemStackObj = asNMSCopy.invoke(null, item);

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

            item = (ItemStack) asBukkitCopy.invoke(null, nmsItemStackObj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    public static Object readItemStackNBT(ItemStack item, String tagName, String tagTypeName) {
        try {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
            Class<?> nbtCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

            Method asNMSCopy = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
            Method nmsItemHasTag = nmsItemStackClass.getDeclaredMethod("hasTag");
            Method nmsItemGetTag = nmsItemStackClass.getDeclaredMethod("getTag");
            Method nbtGetValue = nbtCompoundClass.getDeclaredMethod("get" + tagTypeName, String.class);

            Object nmsItemStackObj = asNMSCopy.invoke(null, item);

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
            return nbtGetValue.invoke(compound, tagName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack setItemStackUnbreakable(ItemStack item) {
        return manipulateItemStackNBT(item, "Unbreakable", true, boolean.class, "Boolean");
    }
}
