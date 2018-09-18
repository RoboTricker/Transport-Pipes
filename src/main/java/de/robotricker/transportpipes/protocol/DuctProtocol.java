package de.robotricker.transportpipes.protocol;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.RelLoc;
import de.robotricker.transportpipes.utils.staticutils.ReflectionUtils;

public class DuctProtocol {

    private static final WrappedDataWatcher.Serializer intSerializer = WrappedDataWatcher.Registry.get(Integer.class);
    private static final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
    private static final WrappedDataWatcher.Serializer vectorSerializer = WrappedDataWatcher.Registry.get(ReflectionUtils.getVector3fClass());
    private static final WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
    private static int nextEntityID = 99999;
    private static UUID uuid = UUID.randomUUID();

    public void sendASD(Player p, BlockLoc blockLoc, RelLoc offset, ArmorStandData asd) {

        int serverVersion = ReflectionUtils.gatherProtocolVersion();

        try {
            if (asd.getEntityID() == -1) {
                asd.setEntityID(++nextEntityID);
            }

            // SPAWN ENTITY
            WrapperPlayServerSpawnEntity spawnWrapper = new WrapperPlayServerSpawnEntity();
            spawnWrapper.setEntityID(asd.getEntityID());
            spawnWrapper.setUniqueId(uuid);
            spawnWrapper.setType(78); // object id: ArmorStand (http://wiki.vg/Protocol#Spawn_Object)
            spawnWrapper.setX(blockLoc.getX() + asd.getRelLoc().getDoubleX() + offset.getDoubleX());
            spawnWrapper.setY(blockLoc.getY() + asd.getRelLoc().getDoubleY() + offset.getDoubleY());
            spawnWrapper.setZ(blockLoc.getZ() + asd.getRelLoc().getDoubleZ() + offset.getDoubleZ());
            spawnWrapper.setOptionalSpeedX(0);
            spawnWrapper.setOptionalSpeedY(0);
            spawnWrapper.setOptionalSpeedZ(0);
            spawnWrapper.setPitch(0);

            double x = asd.getDirection().getX();
            double z = asd.getDirection().getZ();

            double theta = Math.atan2(-x, z);
            double yaw = Math.toDegrees((theta + 2 * Math.PI) % (2 * Math.PI));

            spawnWrapper.setYaw((float) yaw);
            spawnWrapper.setObjectData(0); // without random velocity
            spawnWrapper.sendPacket(p);

            // ENTITYMETADATA
            WrapperPlayServerEntityMetadata metaWrapper = new WrapperPlayServerEntityMetadata();
            metaWrapper.setEntityID(asd.getEntityID());

            byte bitMask = (byte) ((asd.isSmall() ? 0x01 : 0x00) | 0x04 | 0x08 | 0x10); // (small) + hasArms + noBasePlate + Marker

            List<WrappedWatchableObject> metaList = new ArrayList<>();
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer), false));// customname
            // invisible
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(serverVersion <= 110 ? 10 : 11, byteSerializer), bitMask));// armorstand
            // specific
            // data...
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20)));// invisible
            // (entity
            // specific
            // data)
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(serverVersion <= 110 ? 11 : 12, vectorSerializer), ReflectionUtils.createVector3f((float) asd.getHeadRotation().getX(), (float) asd.getHeadRotation().getY(), (float) asd.getHeadRotation().getZ())));// head rot
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(serverVersion <= 110 ? 14 : 15, vectorSerializer), ReflectionUtils.createVector3f((float) asd.getArmRotation().getX(), (float) asd.getArmRotation().getY(), (float) asd.getArmRotation().getZ())));// right arm rot

            metaWrapper.setMetadata(metaList);
            metaWrapper.sendPacket(p);

            // ENTITYEQUIPMENT
            final WrapperPlayServerEntityEquipment equipmentWrapper = new WrapperPlayServerEntityEquipment();
            equipmentWrapper.setEntityID(asd.getEntityID());

            // HAND ITEM
            if (asd.getHandItem() != null) {
                equipmentWrapper.setSlot(EnumWrappers.ItemSlot.MAINHAND);
                equipmentWrapper.setItem(asd.getHandItem());
            }

            // HEAD ITEM
            if (asd.getHeadItem() != null) {
                equipmentWrapper.setSlot(EnumWrappers.ItemSlot.HEAD);
                equipmentWrapper.setItem(asd.getHeadItem());
            }

            // ENTITYMETADATA 2 (fire)
            final WrapperPlayServerEntityMetadata meta2Wrapper = new WrapperPlayServerEntityMetadata();
            meta2Wrapper.setEntityID(asd.getEntityID());

            List<WrappedWatchableObject> meta2List = new ArrayList<>();
            meta2List.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) (0x01 | 0x20)));// on
            // fire
            meta2Wrapper.setMetadata(meta2List);

            TransportPipes.instance.getTPThread().runTask(() -> {
                try {
                    meta2Wrapper.sendPacket(p);
                    equipmentWrapper.sendPacket(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendASD(Player p, BlockLoc blockLoc, List<ArmorStandData> armorStandData) {
        for (ArmorStandData asd : armorStandData) {
            sendASD(p, blockLoc, new RelLoc(0d, 0d, 0d), asd);
        }
    }

    public void removeASD(Player p, List<ArmorStandData> armorStandData) {
        WrapperPlayServerEntityDestroy destroyWrapper = new WrapperPlayServerEntityDestroy();
        int[] ids = armorStandData.stream().mapToInt(ArmorStandData::getEntityID).toArray();
        destroyWrapper.setEntityIds(ids);
        destroyWrapper.sendPacket(p);
    }

}
