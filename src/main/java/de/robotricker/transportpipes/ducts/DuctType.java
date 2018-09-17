package de.robotricker.transportpipes.ducts;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.TransportPipes;

public class DuctType {

    private static List<DuctType> values = new ArrayList<>();

    private String name;

    public DuctType(String displayName) {
        this.name = displayName;
    }

    public String getName() {
        return name;
    }

    public boolean is(String name){
        return this.name.equalsIgnoreCase(name);
    }

    public static List<DuctType> values(){
        return values;
    }

    public static DuctType valueOf(String displayName) {
        for (DuctType dt : values) {
            if (dt.name.equalsIgnoreCase(displayName)) {
                return dt;
            }
        }
        return null;
    }

    public static void registerDuctType(DuctType ductType) {
        values().add(ductType);
        TransportPipes.instance.getRenderSystems().put(ductType, new ArrayList<>());
    }

}
