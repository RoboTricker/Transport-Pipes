package de.robotricker.transportpipes.ducts.types;

import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.robotricker.transportpipes.ItemService;

public class DuctType {

    private BaseDuctType baseDuctType;
    private String name;
    private ItemStack item;
    private String colorCode;
    private Set<DuctType> connectables;

    public DuctType(String name, ItemStack item, String colorCode) {
        this.name = name;
        this.item = item;
        this.colorCode = colorCode;
        this.connectables = new HashSet<>();
    }

    public DuctType connectTo(String... ductTypeNames) {
        for (String name : ductTypeNames) {
            connectables.add(getBaseDuctType().ductTypeValueOf(name));
        }
        return this;
    }

    public DuctType connectToAll() {
        connectables.addAll(getBaseDuctType().ductTypeValues());
        return this;
    }

    public DuctType connectToClasses(Class<? extends DuctType> clazz) {
        for (DuctType dt : getBaseDuctType().ductTypeValues()) {
            if (clazz.isAssignableFrom(dt.getClass())) {
                connectables.add(dt);
            }
        }
        return this;
    }

    public DuctType disconnectFrom(String... ductTypeNames) {
        for (String name : ductTypeNames) {
            connectables.remove(getBaseDuctType().ductTypeValueOf(name));
        }
        return this;
    }

    public DuctType disconnectFromClasses(Class<? extends DuctType> clazz) {
        for (DuctType dt : getBaseDuctType().ductTypeValues()) {
            if (clazz.isAssignableFrom(dt.getClass())) {
                connectables.remove(dt);
            }
        }
        return this;
    }

    public void initItem(ItemService itemService) {
        this.item = itemService.changeDisplayName(itemService.setDuctNBTTags(this, item), getFormattedTypeName());
    }

    public BaseDuctType getBaseDuctType() {
        return baseDuctType;
    }

    public void setBaseDuctType(BaseDuctType baseDuctType) {
        this.baseDuctType = baseDuctType;
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

    public Set<DuctType> getConnectables() {
        return connectables;
    }

    public boolean connectsTo(DuctType otherDuctType) {
        return baseDuctType.equals(otherDuctType.baseDuctType) && connectables.contains(otherDuctType) && otherDuctType.connectables.contains(this);
    }

    @Override
    public String toString() {
        return "DuctType: " + name + "\n" +
                "Connectables: " + connectables.stream().map(dt -> dt.name).collect(Collectors.joining(", "));
    }
}
