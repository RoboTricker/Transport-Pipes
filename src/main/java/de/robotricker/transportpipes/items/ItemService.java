package de.robotricker.transportpipes.items;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.utils.NMSUtils;

public class ItemService {

    private ItemStack wrench;
    private YamlConfiguration tempConf;

    @Inject
    public ItemService(GeneralConf generalConf) {
        Material wrenchMaterial = Material.getMaterial(generalConf.getWrenchItem().toUpperCase(Locale.ENGLISH));
        Objects.requireNonNull(wrenchMaterial, "The material for the wrench item set in the config file is not valid.");

        wrench = generalConf.getWrenchGlowing() ? createGlowingItem(wrenchMaterial) : new ItemStack(wrenchMaterial);
        wrench = changeDisplayNameAndLoreConfig(wrench, LangConf.Key.WRENCH.getLines());
        tempConf = new YamlConfiguration();
    }

    public ItemStack getWrench() {
        return wrench;
    }

    public boolean isWrench(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        return item.getItemMeta().getDisplayName().equals(LangConf.Key.WRENCH.getLines().get(0));
    }

    public ItemStack createModelledItem(int damage) {
        ItemStack woodenPickage = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta meta = woodenPickage.getItemMeta();

        ((Damageable) meta).setDamage(damage);
        meta.setUnbreakable(true);
        woodenPickage.setItemMeta(meta);

        return woodenPickage;
    }

    public ItemStack createGlowingItem(Material material) {
        ItemStack is = new ItemStack(material);
        ItemMeta im = is.getItemMeta();
        im.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(im);
        return is;
    }

    public ItemStack changeDisplayName(ItemStack is, String displayName) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(displayName);
        is.setItemMeta(im);
        return is;
    }

    public ItemStack changeDisplayNameAndLore(ItemStack is, String... displayNameAndLore) {
        ItemMeta meta = is.getItemMeta();
        if (displayNameAndLore.length > 0)
            meta.setDisplayName(displayNameAndLore[0]);
        if (displayNameAndLore.length > 1)
            meta.setLore(Arrays.asList(displayNameAndLore).subList(1, displayNameAndLore.length));
        is.setItemMeta(meta);
        return is;
    }

    public ItemStack changeDisplayNameAndLoreConfig(ItemStack is, String displayName, List<String> lore) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }

    public ItemStack changeDisplayNameAndLoreConfig(ItemStack is, List<String> displayNameAndLore) {
        ItemMeta meta = is.getItemMeta();
        if (displayNameAndLore.size() > 0)
            meta.setDisplayName(displayNameAndLore.get(0));
        if (displayNameAndLore.size() > 1)
            meta.setLore(displayNameAndLore.subList(1, displayNameAndLore.size()));
        is.setItemMeta(meta);
        return is;
    }

    public ItemStack createHeadItem(String uuid, String textureValue, String textureSignature) {
        WrappedGameProfile wrappedProfile = new WrappedGameProfile(UUID.fromString(uuid), null);
        wrappedProfile.getProperties().put("textures", new WrappedSignedProperty("textures", textureValue, textureSignature));

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        sm.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));

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

    public ItemStack setDuctNBTTags(DuctType dt, ItemStack item) {
        item = NMSUtils.manipulateItemStackNBT(item, "basicDuctType", dt.getBaseDuctType().getName(), String.class, "String");
        item = NMSUtils.manipulateItemStackNBT(item, "ductType", dt.getName(), String.class, "String");
        return item;
    }

    public DuctType readDuctNBTTags(ItemStack item, DuctRegister ductRegister) {
        String basicDuctTypeSerialized = (String) NMSUtils.readItemStackNBT(item, "basicDuctType", "String");
        if (basicDuctTypeSerialized != null && !basicDuctTypeSerialized.isEmpty()) {
            BaseDuctType bdt = ductRegister.baseDuctTypeOf(basicDuctTypeSerialized);
            String ductTypeSerialized = (String) NMSUtils.readItemStackNBT(item, "ductType", "String");
            if (ductTypeSerialized != null && !ductTypeSerialized.isEmpty()) {
                return bdt.ductTypeOf(ductTypeSerialized);
            }
        } else {
            //legacy ductType loading (with the gameprofile uuids of the skulls)
            if (item.getItemMeta() instanceof SkullMeta) {

                SkullMeta sm = (SkullMeta) item.getItemMeta();
                try {

                    Field profileField = sm.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    Object profile = profileField.get(sm);
                    if (profile == null)
                        return null;

                    Field idField = Class.forName("com.mojang.authlib.GameProfile").getDeclaredField("id");
                    idField.setAccessible(true);
                    UUID id = (UUID) idField.get(profile);
                    if (id == null)
                        return null;

                    for (DuctType dt : ductRegister.baseDuctTypeOf("pipe").ductTypes()) {
                        Object dtProfile = profileField.get(dt.getBaseDuctType().getItemManager().getItem(dt).getItemMeta());
                        UUID dtId = (UUID) idField.get(dtProfile);

                        if (id.compareTo(dtId) == 0) {
                            return dt;
                        }
                    }
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    public void populateInventoryLine(Inventory inv, int row, ItemStack... items) {
        for (int i = 0; i < 9; i++) {
            if (items.length > i && items[i] != null) {
                ItemStack is = items[i];
                inv.setItem(row * 9 + i, is);
            }
        }
    }

    public ItemStack createWildcardItem(Material material) {
        ItemStack glassPane = new ItemStack(material);
        return changeDisplayNameAndLore(glassPane, ChatColor.RESET.toString());
    }

    public ItemStack createBarrierItem() {
        return changeDisplayNameAndLore(new ItemStack(Material.BARRIER), ChatColor.RESET.toString());
    }

    public boolean isItemWildcardOrBarrier(ItemStack item) {
        if (item != null) {
            switch (item.getType()) {
                case GRAY_STAINED_GLASS_PANE:
                case BLACK_STAINED_GLASS_PANE:
                case RED_STAINED_GLASS_PANE:
                case BLUE_STAINED_GLASS_PANE:
                case LIME_STAINED_GLASS_PANE:
                case WHITE_STAINED_GLASS_PANE:
                case YELLOW_STAINED_GLASS_PANE:
                case BROWN_STAINED_GLASS_PANE:
                case CYAN_STAINED_GLASS_PANE:
                case GREEN_STAINED_GLASS_PANE:
                case LIGHT_BLUE_STAINED_GLASS_PANE:
                case LIGHT_GRAY_STAINED_GLASS_PANE:
                case MAGENTA_STAINED_GLASS_PANE:
                case ORANGE_STAINED_GLASS_PANE:
                case PINK_STAINED_GLASS_PANE:
                case PURPLE_STAINED_GLASS_PANE:
                case BARRIER:
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        return item.getItemMeta().getDisplayName().equals(ChatColor.RESET.toString());
                    }
                default:
                    return false;
            }
        }
        return false;
    }

    public String serializeItemStack(ItemStack itemStack) {
        tempConf.set("itemStack", itemStack);
        String string = tempConf.saveToString();
        tempConf.set("itemStack", null);
        return string;
    }

    public ItemStack deserializeItemStack(String string) {
        try {
            tempConf.loadFromString(string);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ItemStack itemStack = tempConf.getItemStack("itemStack");
        tempConf.set("itemStack", null);
        return itemStack;
    }

    public ShapedRecipe createShapedRecipe(TransportPipes transportPipes, String recipeKey, ItemStack resultItem, String[] shape, Object... ingredientMap) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(transportPipes, recipeKey), resultItem);
        recipe.shape(shape);
        for (int i = 0; i < ingredientMap.length; i += 2) {
            char c = (char) ingredientMap[i];
            if (ingredientMap[i + 1] instanceof Material) {
                recipe.setIngredient(c, (Material) ingredientMap[i + 1]);
            } else {
                recipe.setIngredient(c, new RecipeChoice.MaterialChoice(((Collection<Material>) ingredientMap[i + 1]).stream().collect(Collectors.toList())));
            }
        }
        return recipe;
    }

    public ShapelessRecipe createShapelessRecipe(TransportPipes transportPipes, String recipeKey, ItemStack resultItem, Material... ingredients) {
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(transportPipes, recipeKey), resultItem);
        for (int i = 0; i < ingredients.length; i += 2) {
            recipe.addIngredient(ingredients[i]);
        }
        return recipe;
    }

}
