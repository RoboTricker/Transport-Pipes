package de.robotricker.transportpipes.utils.staticutils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

    public static ItemStack createModelledItem(int damage){
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

}
