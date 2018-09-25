package de.robotricker.transportpipes.utils.staticutils;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;

public class ItemUtils {

    public static ItemStack createModelledItem(int damage) {
        ItemStack is = ReflectionUtils.setItemStackUnbreakable(new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage));
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack createGlowingItem(Material material) {
        return createGlowingItem(material, (short) 0);
    }

    public static ItemStack createGlowingItem(Material material, short damage) {
        ItemStack is = new ItemStack(material, 1, damage);
        ItemMeta im = is.getItemMeta();
        im.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack changeDisplayName(ItemStack is, String displayName) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(displayName);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack createSkullItemStack(String uuid, String textureValue, String textureSignature) {
        WrappedGameProfile wrappedProfile = new WrappedGameProfile(UUID.fromString(uuid), null);
        wrappedProfile.getProperties().put("textures", new WrappedSignedProperty("textures", textureValue, textureSignature));

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta sm = (SkullMeta) skull.getItemMeta();

        Field profileField;
        try {
            profileField = sm.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(sm, wrappedProfile.getHandle());
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        skull.setItemMeta(sm);
        return skull;
    }

    public static ItemStack setDuctNBTTags(DuctType dt, ItemStack item) {
        item = ReflectionUtils.manipulateItemStackNBT(item, "basicDuctType", dt.getBasicDuctType().getName(), String.class, "String");
        item = ReflectionUtils.manipulateItemStackNBT(item, "ductType", dt.getName(), String.class, "String");
        return item;
    }

    public static DuctType readDuctNBTTags(ItemStack item) {
        String basicDuctTypeSerialized = (String) ReflectionUtils.readItemStackNBT(item, "basicDuctType", "String");
        if (basicDuctTypeSerialized != null && !basicDuctTypeSerialized.isEmpty()) {
            BasicDuctType bdt = BasicDuctType.valueOf(basicDuctTypeSerialized);
            String ductTypeSerialized = (String) ReflectionUtils.readItemStackNBT(item, "ductType", "String");
            if (ductTypeSerialized != null && !ductTypeSerialized.isEmpty()) {
                return bdt.ductTypeValueOf(ductTypeSerialized);
            }
        }
        return null;
    }

}