package de.robotricker.transportpipes.saving;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.NBTUtil;

import org.bukkit.World;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;

public class DuctLoader {

    @Inject
    private GlobalDuctManager globalDuctManager;
    @Inject
    private DuctRegister ductRegister;
    @Inject
    private ItemService itemService;

    public void loadDuctsSync(World world, CompoundTag compoundTag) {
        ListTag<CompoundTag> listTag = (ListTag<CompoundTag>) compoundTag.getListTag("ducts");

        synchronized (globalDuctManager.getDucts()) {
            Map<Duct, CompoundTag> ductCompoundTagMap = new HashMap<>();
            for (CompoundTag ductTag : listTag) {
                DuctType ductType = ductRegister.loadDuctTypeFromNBTTag(ductTag);
                BlockLocation blockLoc = ductRegister.loadBlockLocFromNBTTag(ductTag);
                if (ductType == null || blockLoc == null) {
                    continue;
                }
                Duct duct = globalDuctManager.createDuctObject(ductType, blockLoc, world, blockLoc.toLocation(world).getChunk());
                globalDuctManager.registerDuct(duct);
                ductCompoundTagMap.put(duct, ductTag);
            }
            // load duct specific nbt stuff later in order to be able to access other ducts inside this load process
            for (Duct duct : ductCompoundTagMap.keySet()) {
                globalDuctManager.updateDuctConnections(duct);
                duct.loadFromNBTTag(ductCompoundTagMap.get(duct), itemService);
                globalDuctManager.registerDuctInRenderSystems(duct, false);
            }
        }
    }

}
