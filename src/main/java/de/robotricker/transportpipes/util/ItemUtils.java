package de.robotricker.transportpipes.util;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class ItemUtils {

    private ItemUtils() {
    }

    public static ItemStack createModelledItem(int damage) {
        ItemStack item = NMSUtils.setItemStackUnbreakable(new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage));
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGlowingItem(Material material) {
        return createGlowingItem(material, (short) 0);
    }

    public static ItemStack createGlowingItem(Material material, short damage) {
        ItemStack item = new ItemStack(material, 1, damage);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack changeDisplayName(ItemStack item, String displayName) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createSkullItemStack(String uniqueId, String textureValue, String textureSignature) {
        WrappedGameProfile wrappedProfile = new WrappedGameProfile(UUID.fromString(uniqueId), null);
        wrappedProfile.getProperties().put("textures", new WrappedSignedProperty("textures", textureValue, textureSignature));

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        try {
            FieldUtils.writeDeclaredField(meta, "profile", wrappedProfile.getHandle());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack setDuctNBTTags(DuctType ductType, ItemStack item) {
        item = NMSUtils.manipulateItemStackNBT(item, "basicDuctType", ductType.getBasicDuctType().getName(), String.class, "String");
        item = NMSUtils.manipulateItemStackNBT(item, "ductType", ductType.getName(), String.class, "String");
        return item;
    }

    public static DuctType readDuctNBTTags(ItemStack item) {
        String basicDuctTypeSerialized = (String) NMSUtils.readItemStackNBT(item, "basicDuctType", "String");
        if (basicDuctTypeSerialized != null && !basicDuctTypeSerialized.isEmpty()) {
            BaseDuctType ductBaseType = BaseDuctType.valueOf(basicDuctTypeSerialized);
            String ductTypeSerialized = (String) NMSUtils.readItemStackNBT(item, "ductType", "String");
            if (ductTypeSerialized != null && !ductTypeSerialized.isEmpty()) {
                return ductBaseType.ductTypeValueOf(ductTypeSerialized);
            }
        }
        return null;
    }
}
