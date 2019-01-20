package de.robotricker.transportpipes.protocol;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerRelEntityMove;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.utils.NMSUtils;

public class ProtocolService {

    private WrappedDataWatcher.Serializer INT_SERIALIZER;
    private WrappedDataWatcher.Serializer BYTE_SERIALIZER;
    private WrappedDataWatcher.Serializer VECTOR_SERIALIZER;
    private WrappedDataWatcher.Serializer BOOLEAN_SERIALIZER;

    private TransportPipes plugin;

    @Inject
    public ProtocolService(TransportPipes plugin) {
        INT_SERIALIZER = WrappedDataWatcher.Registry.get(Integer.class);
        BYTE_SERIALIZER = WrappedDataWatcher.Registry.get(Byte.class);
        VECTOR_SERIALIZER = WrappedDataWatcher.Registry.get(NMSUtils.getVector3fClass());
        BOOLEAN_SERIALIZER = WrappedDataWatcher.Registry.get(Boolean.class);

        this.plugin = plugin;
    }

    private int nextEntityID = 99999;
    private UUID uuid = UUID.randomUUID();

    public void sendPipeItem(Player p, PipeItem item) {
        sendASD(p, item.getBlockLoc(), item.getRelativeLocation().clone().add(-0.5d, -0.5d, -0.5d), item.getAsd());
    }

    public void updatePipeItem(Player p, PipeItem item) {
        try {
            WrapperPlayServerRelEntityMove moveWrapper = new WrapperPlayServerRelEntityMove();
            moveWrapper.setEntityID(item.getAsd().getEntityID());
            moveWrapper.setDx((int) ((item.getRelativeLocationDifference().getDoubleX() * 32d) * 128));
            moveWrapper.setDy((int) ((item.getRelativeLocationDifference().getDoubleY() * 32d) * 128));
            moveWrapper.setDz((int) ((item.getRelativeLocationDifference().getDoubleZ() * 32d) * 128));
            moveWrapper.setOnGround(true);
            moveWrapper.sendPacket(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePipeItem(final Player p, PipeItem item) {
        removeASD(p, Collections.singletonList(item.getAsd()));
    }

    public void sendASD(Player p, BlockLocation blockLoc, RelativeLocation offset, ArmorStandData asd) {
        int serverVersion = NMSUtils.gatherProtocolVersion();

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
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, BOOLEAN_SERIALIZER), false));// customname
            // invisible
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(serverVersion <= 110 ? 10 : 11, BYTE_SERIALIZER), bitMask));// armorstand
            // specific
            // data...
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, BYTE_SERIALIZER), (byte) (0x20)));// invisible
            // (entity
            // specific
            // data)
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(serverVersion <= 110 ? 11 : 12, VECTOR_SERIALIZER), NMSUtils.createVector3f((float) asd.getHeadRotation().getX(), (float) asd.getHeadRotation().getY(), (float) asd.getHeadRotation().getZ())));// head rot
            metaList.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(serverVersion <= 110 ? 14 : 15, VECTOR_SERIALIZER), NMSUtils.createVector3f((float) asd.getArmRotation().getX(), (float) asd.getArmRotation().getY(), (float) asd.getArmRotation().getZ())));// right arm rot

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
            meta2List.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, BYTE_SERIALIZER), (byte) (0x01 | 0x20)));// on
            // fire
            meta2Wrapper.setMetadata(meta2List);

            plugin.runTaskAsync(() -> {
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

    public void sendASD(Player p, BlockLocation blockLoc, List<ArmorStandData> armorStandData) {
        for (ArmorStandData asd : armorStandData) {
            sendASD(p, blockLoc, new RelativeLocation(0d, 0d, 0d), asd);
        }
    }

    public void removeASD(Player p, List<ArmorStandData> armorStandData) {
        WrapperPlayServerEntityDestroy destroyWrapper = new WrapperPlayServerEntityDestroy();
        int[] ids = armorStandData.stream().mapToInt(ArmorStandData::getEntityID).toArray();
        destroyWrapper.setEntityIds(ids);
        destroyWrapper.sendPacket(p);
    }

}
