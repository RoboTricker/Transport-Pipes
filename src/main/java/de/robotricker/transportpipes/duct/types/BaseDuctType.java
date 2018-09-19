package de.robotricker.transportpipes.duct.types;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.factory.DuctFactory;
import de.robotricker.transportpipes.location.BlockLocation;

public final class BaseDuctType {

    private static List<BaseDuctType> values = new ArrayList<>();

    private String name;
    private DuctFactory factory;
    private List<DuctType> ductTypeValues;

    public BaseDuctType(String name, DuctFactory ductFactory) {
        this.name = name;
        this.factory = ductFactory;
        ductTypeValues = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean is(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    // ****************************************************
    // DUCT TYPE
    // ****************************************************

    public List<DuctType> ductTypeValues() {
        return ductTypeValues;
    }

    public <T extends DuctType> T ductTypeValueOf(String displayName) {
        for (DuctType dt : ductTypeValues) {
            if (dt.getName().equalsIgnoreCase(displayName)) {
                return (T) dt;
            }
        }
        return null;
    }

    public void registerDuctType(DuctType ductType) {
        ductType.setBasicDuctType(this);
        ductType.initItem();
        ductTypeValues.add(ductType);
    }

    public Duct createDuct(DuctType ductType, BlockLocation blockLoc, Chunk chunk) {
        return factory.createDuct(ductType, blockLoc, chunk);
    }

    // ****************************************************
    // BASIC DUCT TYPE
    // ****************************************************

    public static List<BaseDuctType> values() {
        return values;
    }

    @NotNull
    public static BaseDuctType valueOf(String displayName) {
        for (BaseDuctType bdt : values) {
            if (bdt.name.equalsIgnoreCase(displayName)) {
                return bdt;
            }
        }
        return null;
    }

    public static void registerBasicDuctType(BaseDuctType ductType) {
        values.add(ductType);
    }

}
