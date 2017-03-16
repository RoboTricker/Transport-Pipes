package main.de.robotricker.transportpipes.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import main.de.robotricker.transportpipes.PipeThread;
import main.de.robotricker.transportpipes.pipeitems.PipeItem;
import main.de.robotricker.transportpipes.pipes.Pipe;

public class ArmorStandProtocol {

	private static int nextEntityID = 99999;
	private static UUID uuid = UUID.randomUUID();
	@SuppressWarnings("unused")
	private static final Serializer intSerializer = Registry.get(Integer.class);
	private static final Serializer byteSerializer = Registry.get(Byte.class);
	private static final Serializer vectorSerializer = Registry.get(ReflectionManager.getVector3fClass());
	private static final Serializer booleanSerializer = Registry.get(Boolean.class);

	private static ProtocolManager protocolManager;

	public ArmorStandProtocol(ProtocolManager protocolManager) {
		ArmorStandProtocol.protocolManager = protocolManager;
	}

	public void sendPipe(final Player p, Pipe pipe) {
		for (ArmorStandData asd : pipe.getArmorStandList()) {
			sendArmorStandData(p, pipe.getBlockLoc(), asd, new Vector(0f, 0f, 0f));
		}
	}

	public void removePipe(final Player p, Pipe pipe) {
		int[] ids = new int[pipe.getArmorStandList().size()];
		int i = 0;
		for (ArmorStandData asd : pipe.getArmorStandList()) {
			ids[i] = asd.getEntityID();
			i++;
		}
		removeArmorStandDatas(p, ids);
	}

	public void removePipeItem(final Player p, PipeItem item) {
		int id = item.getArmorStand().getEntityID();
		removeArmorStandDatas(p, new int[] { id });
	}

	/**
	 * not updating Item -> only sending (this is also sent when the player comes near enough to see the item even if the item is already in a pipe)
	 */
	public void sendPipeItem(Player p, PipeItem item) {
		sendArmorStandData(p, item.getBlockLoc(), item.getArmorStand(), new Vector(item.changeRelLoc().getFloatX() - 0.5d, item.changeRelLoc().getFloatY() - 0.5d, item.changeRelLoc().getFloatZ() - 0.5d));
	}

	public void updatePipeItem(Player p, PipeItem item) {
		try {
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
			packet.getIntegers().write(0, item.getArmorStand().getEntityID());
			packet.getIntegers().write(1, (int) ((item.changeRelLocDiff().getFloatX() * 32d) * 128));
			packet.getIntegers().write(2, (int) ((item.changeRelLocDiff().getFloatY() * 32d) * 128));
			packet.getIntegers().write(3, (int) ((item.changeRelLocDiff().getFloatZ() * 32d) * 128));
			packet.getBooleans().write(0, true);
			protocolManager.sendServerPacket(p, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendArmorStandData(final Player p, Location blockLoc, ArmorStandData asd, Vector itemOffset) {

		int serverVersion = ReflectionManager.gatherProtocolVersion();

		try {
			if (asd.getEntityID() == -1) {
				asd.setEntityID(++nextEntityID);
			}

			//SPAWN ENTITY
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
			packet.getIntegers().write(0, asd.getEntityID()); //id
			packet.getSpecificModifier(UUID.class).write(0, uuid);
			packet.getDoubles().write(0, blockLoc.getX() + asd.getLoc().getFloatX() + itemOffset.getX()); //x
			packet.getDoubles().write(1, blockLoc.getY() + asd.getLoc().getFloatY() + itemOffset.getY()); //y
			packet.getDoubles().write(2, blockLoc.getZ() + asd.getLoc().getFloatZ() + itemOffset.getZ()); //z
			packet.getIntegers().write(1, 0); //dirx
			packet.getIntegers().write(2, 0); //diry
			packet.getIntegers().write(3, 0); //dirz
			packet.getIntegers().write(4, 0); //pitch

			double x = asd.getDirection().getX();
			double z = asd.getDirection().getZ();

			double theta = Math.atan2(-x, z);
			double yaw = Math.toDegrees((theta + 2 * Math.PI) % (2 * Math.PI));

			packet.getIntegers().write(5, ReflectionManager.convertYaw((float) yaw)); //yaw
			packet.getIntegers().write(6, 78); //object id: ArmorStand (http://wiki.vg/Protocol#Spawn_Object)
			packet.getIntegers().write(7, 0); //without random velocity
			protocolManager.sendServerPacket(p, packet);

			PacketContainer packetEquipment = null;

			//HAND ITEM
			if (asd.getHandItem() != null) {
				packetEquipment = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
				packetEquipment.getIntegers().write(0, asd.getEntityID());
				packetEquipment.getItemSlots().write(0, ItemSlot.MAINHAND);
				packetEquipment.getItemModifier().write(0, asd.getHandItem());
			}

			//HEAD ITEM
			if (asd.getHeadItem() != null) {
				packetEquipment = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
				packetEquipment.getIntegers().write(0, asd.getEntityID());
				packetEquipment.getItemSlots().write(0, ItemSlot.HEAD);
				packetEquipment.getItemModifier().write(0, asd.getHeadItem());
			}

			final PacketContainer packetEquipmentFinal = packetEquipment;

			//ENTITYMETADATA
			PacketContainer packetMetadata1 = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
			packetMetadata1.getIntegers().write(0, asd.getEntityID()); //Entity ID

			byte bitMask = (byte) ((asd.isSmall() ? 0x01 : 0x00) | 0x04 | 0x08 | 0x10); //(small) + hasArms + noBasePlate + Marker

			List<WrappedWatchableObject> list1 = new ArrayList<>();
			list1.add(new WrappedWatchableObject(new WrappedDataWatcherObject(3, booleanSerializer), false));//customname invisible
			list1.add(new WrappedWatchableObject(new WrappedDataWatcherObject(serverVersion <= 110 ? 10 : 11, byteSerializer), bitMask));//armorstand specific data...
			list1.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20)));//invisible (entity specific data)
			list1.add(new WrappedWatchableObject(new WrappedDataWatcherObject(serverVersion <= 110 ? 11 : 12, vectorSerializer), ReflectionManager.createVector3f((float) asd.getHeadRotation().getX(), (float) asd.getHeadRotation().getY(), (float) asd.getHeadRotation().getZ())));//head rot
			list1.add(new WrappedWatchableObject(new WrappedDataWatcherObject(serverVersion <= 110 ? 14 : 15, vectorSerializer), ReflectionManager.createVector3f((float) asd.getArmRotation().getX(), (float) asd.getArmRotation().getY(), (float) asd.getArmRotation().getZ())));//right arm rot

			packetMetadata1.getWatchableCollectionModifier().write(0, list1);
			protocolManager.sendServerPacket(p, packetMetadata1);

			//ENTITYMETADATA 2 (fire)
			final PacketContainer packetMetadata2 = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
			packetMetadata2.getIntegers().write(0, asd.getEntityID()); //Entity ID
			List<WrappedWatchableObject> list2 = new ArrayList<>();
			list2.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, byteSerializer), (byte) (0x01 | 0x20)));//on fire + invisible (entity specific data)
			packetMetadata2.getWatchableCollectionModifier().write(0, list2);

			PipeThread.runTask(new Runnable() {

				@Override
				public void run() {
					try {
						protocolManager.sendServerPacket(p, packetMetadata2);
						protocolManager.sendServerPacket(p, packetEquipmentFinal);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeArmorStandDatas(Player p, int[] ids) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntegerArrays().write(0, ids);
		try {
			protocolManager.sendServerPacket(p, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
