package de.robotricker.transportpipes.saving;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SaveService {

    @Inject
    private GlobalDuctManager globalDuctManager;
    @Inject
    private DuctRegister ductRegister;

    public void saveDuctsSync() {

        System.out.println("Saving ducts...");

        Map<World, ListTag<CompoundTag>> worldTags = new HashMap<>();
        synchronized (globalDuctManager.getDucts()) {
            for (World world : Bukkit.getWorlds()) {
                ListTag<CompoundTag> listTag = new ListTag<>(CompoundTag.class);
                Map<BlockLocation, Duct> ducts = globalDuctManager.getDucts(world);
                for (BlockLocation bl : ducts.keySet()) {
                    Duct duct = ducts.get(bl);
                    CompoundTag ductTag = new CompoundTag();

                    ductRegister.saveDuctTypeToNBTTag(duct.getDuctType(), ductTag);
                    ductRegister.saveBlockLocToNBTTag(duct.getBlockLoc(), ductTag);
                    duct.saveToNBTTag(ductTag);

                    listTag.add(ductTag);
                }
                if (listTag.size() > 0) {
                    worldTags.put(world, listTag);
                }
            }
        }

        for (World world : Bukkit.getWorlds()) {
            if(worldTags.containsKey(world)) {
                try {
                    NBTUtil.writeTag(worldTags.get(world), Paths.get(world.getWorldFolder().getAbsolutePath(), "ducts.dat").toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Saved ducts.");

    }

    public void loadDuctsSync() {

        System.out.println("Loading ducts...");

        Map<World, ListTag<CompoundTag>> worldTags = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            try {
                ListTag listTag = (ListTag) NBTUtil.readTag(Paths.get(world.getWorldFolder().getAbsolutePath(), "ducts.dat").toFile());
                if(listTag != null && listTag.size() > 0) {
                    worldTags.put(world, listTag);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        synchronized (globalDuctManager.getDucts()) {
            for (World world : worldTags.keySet()) {
                for (CompoundTag ductTag : worldTags.get(world)) {
                    DuctType ductType = ductRegister.loadDuctTypeFromNBTTag(ductTag);
                    BlockLocation blockLoc = ductRegister.loadBlockLocFromNBTTag(ductTag);
                    if(ductType == null || blockLoc == null) {
                        continue;
                    }
                    Duct duct = ductType.getBaseDuctType().getDuctFactory().createDuct(ductType, blockLoc, world, blockLoc.toLocation(world).getChunk());
                    globalDuctManager.getDucts(world).put(blockLoc, duct);
                }
            }
        }

        System.out.println("Loaded ducts.");

    }

}
