package de.robotricker.transportpipes.duct.types;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.util.ItemUtils;

public class DuctType {

    private BaseDuctType basicDuctType;
    private String name;
    private ItemStack item;
    private String colorCode;

    public DuctType(String name, ItemStack item, String colorCode) {
        this.name = name;
        this.item = item;
        this.colorCode = colorCode;
    }

    public void initItem(){
        this.item = ItemUtils.changeDisplayName(ItemUtils.setDuctNBTTags(this, item), getFormattedTypeName());
    }

    public BaseDuctType getBasicDuctType() {
        return basicDuctType;
    }

    public void setBasicDuctType(BaseDuctType basicDuctType) {
        this.basicDuctType = basicDuctType;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean is(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    public String getFormattedTypeName() {
        return colorCode + name;
    }

}
