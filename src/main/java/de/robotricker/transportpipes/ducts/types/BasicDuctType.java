package de.robotricker.transportpipes.ducts.types;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.factory.DuctFactory;
import de.robotricker.transportpipes.utils.BlockLoc;

public final class BasicDuctType {

    private static List<BasicDuctType> values = new ArrayList<>();

    private String name;
    private DuctFactory factory;
    private List<DuctType> ductTypeValues;

    public BasicDuctType(String name, DuctFactory ductFactory) {
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

    public Duct createDuct(DuctType ductType, BlockLoc blockLoc, World world, Chunk chunk) {
        return factory.createDuct(ductType, blockLoc, world, chunk);
    }

    // ****************************************************
    // BASIC DUCT TYPE
    // ****************************************************

    public static List<BasicDuctType> values() {
        return values;
    }

    @NotNull
    public static BasicDuctType valueOf(String displayName) {
        for (BasicDuctType bdt : values) {
            if (bdt.name.equalsIgnoreCase(displayName)) {
                return bdt;
            }
        }
        return null;
    }

    public static void registerBasicDuctType(BasicDuctType ductType) {
        values.add(ductType);
    }

}
