package de.robotricker.transportpipes.ducts.pipe;

import java.util.ArrayList;
import java.util.List;

public class PipeType {

    private static List<PipeType> values = new ArrayList<>();

    private String name;
    private String colorCode;

    public PipeType(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }

    public String getName() {
        return name;
    }

    public boolean is(String name){
        return this.name.equalsIgnoreCase(name);
    }

    public String getFormattedTypeName(){
        return colorCode + name;
    }

    public static List<PipeType> values(){
        return values;
    }

    public static PipeType valueOf(String name) {
        for (PipeType pt : PipeType.values()) {
            if (pt.getName().equalsIgnoreCase(name)) {
                return pt;
            }
        }
        return null;
    }

    public static void registerPipeType(PipeType pipeType) {
        values().add(pipeType);
    }

}
