package de.robotricker.transportpipes.saving;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.NBTUtil;

import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;

public class DuctSaver {

    @Inject
    private GlobalDuctManager globalDuctManager;
    @Inject
    private DuctRegister ductRegister;
    @Inject
    private TransportPipes transportPipes;
    @Inject
    private ItemService itemService;

    public void saveDuctsSync(World world) {
        ListTag<CompoundTag> listTag = new ListTag<>(CompoundTag.class);
        synchronized (globalDuctManager.getDucts()) {
            Map<BlockLocation, Duct> ducts = globalDuctManager.getDucts(world);
            for (BlockLocation bl : ducts.keySet()) {
                Duct duct = ducts.get(bl);
                CompoundTag ductTag = new CompoundTag();

                ductRegister.saveDuctTypeToNBTTag(duct.getDuctType(), ductTag);
                ductRegister.saveBlockLocToNBTTag(duct.getBlockLoc(), ductTag);
                duct.saveToNBTTag(ductTag, itemService);

                listTag.add(ductTag);
            }
            if (listTag.size() == 0) {
                return;
            }
        }

        try {

            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("ducts", listTag);
            compoundTag.putString("version", transportPipes.getDescription().getVersion());

            NBTUtil.writeTag(compoundTag, Paths.get(world.getWorldFolder().getAbsolutePath(), "ducts.dat").toFile(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
