package de.robotricker.transportpipes.ducts.manager;

import org.bukkit.World;

import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.location.BlockLocation;

public abstract class DuctManager<T extends Duct> {

    protected DuctRegister ductRegister;

    @Inject
    public DuctManager(DuctRegister ductRegister) {
        this.ductRegister = ductRegister;
    }

    public abstract void registerDuctTypes();

    public abstract void tick(Map<World, Map<BlockLocation, Duct>> ducts);

}
