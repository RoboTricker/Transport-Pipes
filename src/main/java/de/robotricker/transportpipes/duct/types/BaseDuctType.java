package de.robotricker.transportpipes.duct.types;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.factory.DuctFactory;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.items.ItemManager;
import de.robotricker.transportpipes.rendersystems.ModelledRenderSystem;
import de.robotricker.transportpipes.rendersystems.VanillaRenderSystem;

public final class BaseDuctType<T extends Duct> {

    private String name;
    private DuctManager<T> ductManager;
    private DuctFactory<T> ductFactory;
    private ItemManager<T> itemManager;
    private VanillaRenderSystem vanillaRenderSystem;
    private ModelledRenderSystem modelledRenderSystem;

    private List<DuctType> ductTypes;

    public BaseDuctType(String name, DuctManager<T> ductManager, DuctFactory<T> ductFactory, ItemManager<T> itemManager) {
        this.name = name;
        this.ductManager = ductManager;
        this.ductFactory = ductFactory;
        this.itemManager = itemManager;
        this.ductTypes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public DuctManager<T> getDuctManager() {
        return ductManager;
    }

    public DuctFactory<T> getDuctFactory() {
        return ductFactory;
    }

    public ItemManager<T> getItemManager() {
        return itemManager;
    }

    public VanillaRenderSystem getVanillaRenderSystem() {
        return vanillaRenderSystem;
    }

    public ModelledRenderSystem getModelledRenderSystem() {
        return modelledRenderSystem;
    }

    public void setVanillaRenderSystem(VanillaRenderSystem vanillaRenderSystem) {
        this.vanillaRenderSystem = vanillaRenderSystem;
    }

    public void setModelledRenderSystem(ModelledRenderSystem modelledRenderSystem) {
        this.modelledRenderSystem = modelledRenderSystem;
    }

    public boolean is(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    // ****************************************************
    // DUCT MATERIAL
    // ****************************************************

    public List<DuctType> ductTypes() {
        return ductTypes;
    }

    public <S extends DuctType> S ductTypeOf(String displayName) {
        for (DuctType dt : ductTypes) {
            if (dt.getName().equalsIgnoreCase(displayName)) {
                return (S) dt;
            }
        }
        return null;
    }

    public void registerDuctType(DuctType ductType) {
        if (ductTypes.stream().anyMatch(dt -> dt.getName().equalsIgnoreCase(ductType.getName()))) {
            throw new IllegalArgumentException("DuctType '" + ductType.getName() + "' already exists");
        }
        ductTypes.add(ductType);
    }

}
