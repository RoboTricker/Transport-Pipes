package de.robotricker.transportpipes.ducts;

import java.util.ArrayList;
import java.util.List;

public class DuctType {

    private static List<DuctType> values = new ArrayList<>();

    private String displayName;

    public DuctType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<DuctType> values(){
        return values;
    }

    public static DuctType valueOf(String displayName) {
        for (DuctType dt : values) {
            if (dt.displayName.equalsIgnoreCase(displayName)) {
                return dt;
            }
        }
        return null;
    }

    public static void registerDuctType(DuctType ductType) {
        values().add(ductType);
    }

}
