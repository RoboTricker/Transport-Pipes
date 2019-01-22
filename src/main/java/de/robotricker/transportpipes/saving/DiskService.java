package de.robotricker.transportpipes.saving;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.NBTUtil;

import org.bukkit.World;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;

public class DiskService {

    @Inject
    private TransportPipes transportPipes;
    @Inject
    private DuctSaver ductSaver;

    public void loadDuctsSync(World world) {
        try {
            CompoundTag compoundTag = (CompoundTag) NBTUtil.readTag(Paths.get(world.getWorldFolder().getAbsolutePath(), "ducts.dat").toFile());
            String version = compoundTag.getString("version");

            long versionLong = transportPipes.convertVersionToLong(version);
            DuctLoader ductLoader;
            if (versionLong <= transportPipes.convertVersionToLong("4.3.1")) {
                ductLoader = transportPipes.getInjector().getSingleton(LegacyDuctLoader_v4_3_1.class);
            } else {
                ductLoader = transportPipes.getInjector().getSingleton(DuctLoader.class);
            }

            if (ductLoader == null) {
                throw new IOException("Could not load ducts.dat file because version " + version + " is not supported!");
            }

            ductLoader.loadDuctsSync(world, compoundTag);

        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDuctsSync(World world) {
        ductSaver.saveDuctsSync(world);
    }

}
