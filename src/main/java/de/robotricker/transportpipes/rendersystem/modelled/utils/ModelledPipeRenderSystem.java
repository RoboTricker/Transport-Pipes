package de.robotricker.transportpipes.rendersystem.modelled.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.ProtocolUtils;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeCOLOREDModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeEXTRACTIONModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeGOLDENModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeICEModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeIRONModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeVOIDModel;
import de.robotricker.transportpipes.utils.InventoryUtils;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;

public class ModelledPipeRenderSystem extends RenderSystem {

	public static final String RESOURCEPACK_URL = "https://raw.githubusercontent.com/RoboTricker/Transport-Pipes/master/src/main/resources/TransportPipes-ResourcePack.zip";

	private Map<Pipe, ArmorStandData> pipeMidAsd = new HashMap<>();
	private Map<Pipe, Map<WrappedDirection, ArmorStandData>> pipeConnsAsd = new HashMap<>();
	private AxisAlignedBB pipeMidAABB;
	private Map<WrappedDirection, AxisAlignedBB> pipeConnsAABBs = new HashMap<>();

	private Map<PipeType, ModelledPipeModel> pipeModels = new HashMap<>();

	private List<Player> loadedResourcePackPlayers = new ArrayList<>();

	public ModelledPipeRenderSystem(ArmorStandProtocol protocol) {
		super(protocol);
		pipeModels.put(PipeType.COLORED, new ModelledPipeCOLOREDModel());
		pipeModels.put(PipeType.ICE, new ModelledPipeICEModel());
		pipeModels.put(PipeType.GOLDEN, new ModelledPipeGOLDENModel());
		pipeModels.put(PipeType.IRON, new ModelledPipeIRONModel());
		pipeModels.put(PipeType.VOID, new ModelledPipeVOIDModel());
		pipeModels.put(PipeType.EXTRACTION, new ModelledPipeEXTRACTIONModel());

		pipeMidAABB = new AxisAlignedBB(4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d);
		pipeConnsAABBs.put(WrappedDirection.NORTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 0d / 16d, 12d / 16d, 12d / 16d, 4d / 16d));
		pipeConnsAABBs.put(WrappedDirection.EAST, new AxisAlignedBB(12d / 16d, 4d / 16d, 4d / 16d, 16d / 16d, 12d / 16d, 12d / 16d));
		pipeConnsAABBs.put(WrappedDirection.SOUTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d, 16d / 16d));
		pipeConnsAABBs.put(WrappedDirection.WEST, new AxisAlignedBB(0d / 16d, 4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d));
		pipeConnsAABBs.put(WrappedDirection.UP, new AxisAlignedBB(4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d, 16d / 16d, 12d / 16d));
		pipeConnsAABBs.put(WrappedDirection.DOWN, new AxisAlignedBB(4d / 16d, 0d / 16d, 4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d));
	}

	@Override
	public void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections) {
		if (pipeMidAsd.containsKey(duct)) {
			return;
		}
		Pipe pipe = (Pipe) duct;

		ModelledPipeModel model = pipeModels.get(pipe.getPipeType());
		pipeMidAsd.put(pipe, model.createMidASD(ModelledPipeMidModelData.createModelData(pipe)));
		Map<WrappedDirection, ArmorStandData> connsMap = new HashMap<>();
		pipeConnsAsd.put(pipe, connsMap);
		for (WrappedDirection conn : allConnections) {
			connsMap.put(conn, model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, conn)));
		}

	}

	@Override
	public void updateDuctASD(Duct duct) {
		Pipe pipe = (Pipe) duct;

		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe) || pipeConnsAsd.get(pipe) == null) {
			return;
		}

		List<ArmorStandData> removedASD = new ArrayList<>();
		List<ArmorStandData> addedASD = new ArrayList<>();

		Map<WrappedDirection, ArmorStandData> connsMap = pipeConnsAsd.get(pipe);
		ModelledPipeModel model = pipeModels.get(pipe.getPipeType());

		Collection<WrappedDirection> newConns = pipe.getAllConnections();
		for (WrappedDirection pd : WrappedDirection.values()) {
			if (connsMap.containsKey(pd) && newConns.contains(pd)) {
				// direction was active before and after update
				ArmorStandData newASD = model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, pd));
				if (!connsMap.get(pd).isSimilar(newASD)) {
					// ASD changed after update in this direction
					removedASD.add(connsMap.get(pd));
					addedASD.add(newASD);
					connsMap.put(pd, newASD);
				}
			} else if (!connsMap.containsKey(pd) && newConns.contains(pd)) {
				// direction wasn't active before update but direction IS active after update
				ArmorStandData newASD = model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, pd));
				addedASD.add(newASD);
				connsMap.put(pd, newASD);
			} else if (connsMap.containsKey(pd) && !newConns.contains(pd)) {
				// direction was active before update but isn't active after update
				removedASD.add(connsMap.get(pd));
				connsMap.remove(pd);
			}
		}

		// SEND TO CLIENTS
		List<Player> players = protocol.getAllPlayersWithRenderSystem(this);
		int[] removedIds = ProtocolUtils.convertArmorStandListToEntityIdArray(removedASD);
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
			protocol.sendArmorStandDatas(p, pipe.getBlockLoc(), addedASD);
		}

	}

	@Override
	public void destroyDuctASD(Duct duct) {
		Pipe pipe = (Pipe) duct;

		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe) || pipeConnsAsd.get(pipe) == null) {
			return;
		}
		pipeMidAsd.remove(pipe);
		pipeConnsAsd.remove(pipe);
	}

	@Override
	public List<ArmorStandData> getASDForDuct(Duct duct) {
		Pipe pipe = (Pipe) duct;

		List<ArmorStandData> ASD = new ArrayList<>();
		if (pipeMidAsd.containsKey(pipe)) {
			ASD.add(pipeMidAsd.get(pipe));
		}
		if (pipeConnsAsd.containsKey(pipe)) {
			ASD.addAll(pipeConnsAsd.get(pipe).values());
		}
		return ASD;
	}

	@Override
	public WrappedDirection getClickedDuctFace(Player player, Duct duct) {
		if (duct == null) {
			return null;
		}
		Pipe pipe = (Pipe) duct;

		Vector ray = player.getEyeLocation().getDirection();
		Vector origin = player.getEyeLocation().toVector();

		Collection<WrappedDirection> pipeConns = pipe.getAllConnections();
		WrappedDirection clickedMidFace = pipeMidAABB.rayIntersection(ray, origin, pipe.getBlockLoc());
		if (clickedMidFace != null && !pipeConns.contains(clickedMidFace)) {
			return clickedMidFace;
		} else {
			double nearestDistanceSquared = Double.MAX_VALUE;
			WrappedDirection currentClickedConnFace = null;
			for (WrappedDirection pd : pipeConns) {
				AxisAlignedBB connAABB = pipeConnsAABBs.get(pd);
				double newDistanceSquared = connAABB.getAABBMiddle(pipe.getBlockLoc()).distanceSquared(origin);
				if (newDistanceSquared < nearestDistanceSquared) {
					WrappedDirection clickedConnFace = connAABB.rayIntersection(ray, origin, pipe.getBlockLoc());
					if (clickedConnFace != null) {
						nearestDistanceSquared = newDistanceSquared;
						currentClickedConnFace = clickedConnFace;
					}
				}
			}
			return currentClickedConnFace;
		}

	}

	@Override
	public AxisAlignedBB getOuterHitbox(Duct duct) {
		if (duct == null) {
			return null;
		}
		Pipe pipe = (Pipe) duct;

		List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
		aabbs.add(pipeMidAABB);
		Collection<WrappedDirection> pipeConns = pipe.getAllConnections();
		for (WrappedDirection pd : pipeConns) {
			aabbs.add(pipeConnsAABBs.get(pd));
		}
		double minx = Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double minz = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE;
		double maxy = Double.MIN_VALUE;
		double maxz = Double.MIN_VALUE;
		for (AxisAlignedBB aabb : aabbs) {
			minx = Math.min(aabb.minx, minx);
			miny = Math.min(aabb.miny, miny);
			minz = Math.min(aabb.minz, minz);
			maxx = Math.max(aabb.maxx, maxx);
			maxy = Math.max(aabb.maxy, maxy);
			maxz = Math.max(aabb.maxz, maxz);
		}
		return new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz);
	}

	@Override
	public int[] getRenderSystemIds() {
		return new int[]{1};
	}

	@Override
	public DuctType getDuctType() {
		return DuctType.PIPE;
	}
	
	@Override
	public void initPlayer(Player p) {
		if (Bukkit.getPluginManager().isPluginEnabled("AuthMe") && !fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(p)) {
			return;
		}
		if (!loadedResourcePackPlayers.contains(p)) {
			p.closeInventory();
			p.setResourcePack(TransportPipes.instance.generalConf.getResourcepackURL());
		}
	}

	@EventHandler
	public void onResourcePackStatus(PlayerResourcePackStatusEvent e) {
		if (e.getStatus() == Status.DECLINED || e.getStatus() == Status.FAILED_DOWNLOAD) {
			RenderSystem beforePm = TransportPipes.instance.armorStandProtocol.getPlayerRenderSystem(e.getPlayer(), DuctType.PIPE);
			if (beforePm.equals(this)) {
				TransportPipes.instance.armorStandProtocol.changePlayerRenderSystem(e.getPlayer(), 0);
			}
			e.getPlayer().sendMessage("§cResourcepack Download failed: Switched to the Vanilla Model System");
			e.getPlayer().sendMessage("§cDid you enable \"Server Resourcepacks\" in your server list?");
		} else {
			if (!loadedResourcePackPlayers.contains(e.getPlayer())) {
				loadedResourcePackPlayers.add(e.getPlayer());
			}
		}
	}

	public class AuthMeLoginListener implements Listener {

		@EventHandler
		public void onAuthMeLogin(fr.xephi.authme.events.LoginEvent e) {
			RenderSystem beforePm = TransportPipes.instance.armorStandProtocol.getPlayerRenderSystem(e.getPlayer(), DuctType.PIPE);
			if (beforePm.equals(ModelledPipeRenderSystem.this) && !loadedResourcePackPlayers.contains(e.getPlayer())) {
				initPlayer(e.getPlayer());
			}
		}
	}

}
