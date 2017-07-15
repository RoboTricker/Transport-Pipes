package de.robotricker.transportpipes.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerRelEntityMove;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.rendersystem.PipeRenderSystem;
import de.robotricker.transportpipes.settings.SettingsUtils;

public class ArmorStandProtocol {

	private static int nextEntityID = 99999;
	private static UUID uuid = UUID.randomUUID();

	@SuppressWarnings("unused")
	private static final Serializer intSerializer = Registry.get(Integer.class);
	private static final Serializer byteSerializer = Registry.get(Byte.class);
	private static final Serializer vectorSerializer = Registry.get(ReflectionManager.getVector3fClass());
	private static final Serializer booleanSerializer = Registry.get(Boolean.class);

	public PipeRenderSystem getPlayerPipeRenderSystem(Player p) {
		return SettingsUtils.loadPlayerSettings(p).getRenderSystem();
	}

	public List<Player> getAllPlayersWithPipeManager(PipeRenderSystem renderSystem) {
		List<Player> players = new ArrayList<>();
		players.addAll(Bukkit.getOnlinePlayers());

		Iterator<Player> it = players.iterator();
		while (it.hasNext()) {
			Player p = it.next();
			//remove all players which don't use the given PipeRenderSystem
			if (!getPlayerPipeRenderSystem(p).equals(renderSystem)) {
				it.remove();
			}
		}
		return players;
	}

	public void removePipeItem(final Player p, PipeItem item) {
		int id = item.getArmorStand().getEntityID();
		removeArmorStandDatas(p, new int[] { id });
	}

	/**
	 * not updating Item -> only sending (this is also sent when the player comes near enough to see the item even if the item is already in a pipe)
	 */
	public void sendPipeItem(Player p, PipeItem item) {
		sendArmorStandData(p, item.getBlockLoc(), item.getArmorStand(), new Vector(item.relLoc().getFloatX() - 0.5d, item.relLoc().getFloatY() - 0.5d, item.relLoc().getFloatZ() - 0.5d));
	}

	public void updatePipeItem(Player p, PipeItem item) {
		try {
			WrapperPlayServerRelEntityMove moveWrapper = new WrapperPlayServerRelEntityMove();
			moveWrapper.setEntityID(item.getArmorStand().getEntityID());
			moveWrapper.setDx((int) ((item.relLocDerivation().getFloatX() * 32d) * 128));
			moveWrapper.setDy((int) ((item.relLocDerivation().getFloatY() * 32d) * 128));
			moveWrapper.setDz((int) ((item.relLocDerivation().getFloatZ() * 32d) * 128));
			moveWrapper.setOnGround(true);
			moveWrapper.sendPacket(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void changePipeRenderSystem(Player p, PipeRenderSystem newRenderSystem) {
		//despawn all old pipes
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(p.getWorld());
		if (pipeMap != null) {
			synchronized (pipeMap) {
				for (Pipe pipe : pipeMap.values()) {
					TransportPipes.pipePacketManager.despawnPipe(p, pipe);
				}
			}
		}

		//change render system
		SettingsUtils.loadPlayerSettings(p).setRenderSystem(newRenderSystem.getRenderSystemId());
		newRenderSystem.initPlayer(p);

		//spawn all new pipes
		if (pipeMap != null) {
			synchronized (pipeMap) {
				for (Pipe pipe : pipeMap.values()) {
					TransportPipes.pipePacketManager.spawnPipe(p, pipe);
				}
			}
		}
	}

	public void sendArmorStandData(final Player p, Location blockLoc, ArmorStandData asd, Vector itemOffset) {

		int serverVersion = ReflectionManager.gatherProtocolVersion();

		try {
			if (asd.getEntityID() == -1) {
				asd.setEntityID(++nextEntityID);
			}

			//SPAWN ENTITY
			WrapperPlayServerSpawnEntity spawnWrapper = new WrapperPlayServerSpawnEntity();
			spawnWrapper.setEntityID(asd.getEntityID());
			spawnWrapper.setUniqueId(uuid);
			spawnWrapper.setType(78); //object id: ArmorStand (http://wiki.vg/Protocol#Spawn_Object)
			spawnWrapper.setX(blockLoc.getX() + asd.getLoc().getFloatX() + itemOffset.getX());
			spawnWrapper.setY(blockLoc.getY() + asd.getLoc().getFloatY() + itemOffset.getY());
			spawnWrapper.setZ(blockLoc.getZ() + asd.getLoc().getFloatZ() + itemOffset.getZ());
			spawnWrapper.setOptionalSpeedX(0);
			spawnWrapper.setOptionalSpeedY(0);
			spawnWrapper.setOptionalSpeedZ(0);
			spawnWrapper.setPitch(0);

			double x = asd.getDirection().getX();
			double z = asd.getDirection().getZ();

			double theta = Math.atan2(-x, z);
			double yaw = Math.toDegrees((theta + 2 * Math.PI) % (2 * Math.PI));

			spawnWrapper.setYaw((float) yaw);
			spawnWrapper.setObjectData(0); //without random velocity
			spawnWrapper.sendPacket(p);

			//ENTITYMETADATA
			WrapperPlayServerEntityMetadata metaWrapper = new WrapperPlayServerEntityMetadata();
			metaWrapper.setEntityID(asd.getEntityID());

			byte bitMask = (byte) ((asd.isSmall() ? 0x01 : 0x00) | 0x04 | 0x08 | 0x10); //(small) + hasArms + noBasePlate + Marker

			List<WrappedWatchableObject> metaList = new ArrayList<>();
			metaList.add(new WrappedWatchableObject(new WrappedDataWatcherObject(3, booleanSerializer), false));//customname invisible
			metaList.add(new WrappedWatchableObject(new WrappedDataWatcherObject(serverVersion <= 110 ? 10 : 11, byteSerializer), bitMask));//armorstand specific data...
			metaList.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20)));//invisible (entity specific data)
			metaList.add(new WrappedWatchableObject(new WrappedDataWatcherObject(serverVersion <= 110 ? 11 : 12, vectorSerializer), ReflectionManager.createVector3f((float) asd.getHeadRotation().getX(), (float) asd.getHeadRotation().getY(), (float) asd.getHeadRotation().getZ())));//head rot
			metaList.add(new WrappedWatchableObject(new WrappedDataWatcherObject(serverVersion <= 110 ? 14 : 15, vectorSerializer), ReflectionManager.createVector3f((float) asd.getArmRotation().getX(), (float) asd.getArmRotation().getY(), (float) asd.getArmRotation().getZ())));//right arm rot

			metaWrapper.setMetadata(metaList);
			metaWrapper.sendPacket(p);

			//ENTITYEQUIPMENT
			final WrapperPlayServerEntityEquipment equipmentWrapper = new WrapperPlayServerEntityEquipment();
			equipmentWrapper.setEntityID(asd.getEntityID());

			//HAND ITEM
			if (asd.getHandItem() != null) {
				equipmentWrapper.setSlot(ItemSlot.MAINHAND);
				equipmentWrapper.setItem(asd.getHandItem());
			}

			//HEAD ITEM
			if (asd.getHeadItem() != null) {
				equipmentWrapper.setSlot(ItemSlot.HEAD);
				equipmentWrapper.setItem(asd.getHeadItem());
			}

			//ENTITYMETADATA 2 (fire)
			final WrapperPlayServerEntityMetadata meta2Wrapper = new WrapperPlayServerEntityMetadata();
			meta2Wrapper.setEntityID(asd.getEntityID());

			List<WrappedWatchableObject> meta2List = new ArrayList<>();
			meta2List.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, byteSerializer), (byte) (0x01 | 0x20)));//on fire + invisible (entity specific data)
			meta2Wrapper.setMetadata(meta2List);

			PipeThread.runTask(new Runnable() {

				@Override
				public void run() {
					try {
						meta2Wrapper.sendPacket(p);
						equipmentWrapper.sendPacket(p);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendArmorStandDatas(Player p, Location blockLoc, List<ArmorStandData> asd) {
		for (ArmorStandData anAsd : asd) {
			sendArmorStandData(p, blockLoc, anAsd, new Vector(0f, 0f, 0f));
		}
	}

	public void removeArmorStandDatas(Player p, int[] ids) {
		WrapperPlayServerEntityDestroy destroyWrapper = new WrapperPlayServerEntityDestroy();
		destroyWrapper.setEntityIds(ids);
		destroyWrapper.sendPacket(p);
	}

}
