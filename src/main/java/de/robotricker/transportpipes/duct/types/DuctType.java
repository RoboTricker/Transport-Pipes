package de.robotricker.transportpipes.duct.types;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.robotricker.transportpipes.duct.Duct;

public class DuctType {

    private BaseDuctType<? extends Duct> baseDuctType;
    private String name;
    private String displayName;
    private Set<DuctType> connectables;

    public DuctType(BaseDuctType<? extends Duct> baseDuctType, String name, String displayName) {
        this.baseDuctType = baseDuctType;
        this.name = name;
        this.displayName = displayName;
        this.connectables = new HashSet<>();
    }

    public DuctType connectTo(String... ductTypeNames) {
        for (String name : ductTypeNames) {
            connectables.add(getBaseDuctType().ductTypeOf(name));
        }
        return this;
    }

    public DuctType connectToAll() {
        connectables.addAll(getBaseDuctType().ductTypes());
        return this;
    }

    public DuctType connectToClasses(Class<? extends DuctType> clazz) {
        for (DuctType dt : getBaseDuctType().ductTypes()) {
            if (clazz.isAssignableFrom(dt.getClass())) {
                connectables.add(dt);
            }
        }
        return this;
    }

    public DuctType disconnectFrom(String... ductTypeNames) {
        for (String name : ductTypeNames) {
            connectables.remove(getBaseDuctType().ductTypeOf(name));
        }
        return this;
    }

    public DuctType disconnectFromClasses(Class<? extends DuctType> clazz) {
        for (DuctType dt : getBaseDuctType().ductTypes()) {
            if (clazz.isAssignableFrom(dt.getClass())) {
                connectables.remove(dt);
            }
        }
        return this;
    }

    public BaseDuctType<? extends Duct> getBaseDuctType() {
        return baseDuctType;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean is(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    public String getFormattedTypeName() {
        return displayName;
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
