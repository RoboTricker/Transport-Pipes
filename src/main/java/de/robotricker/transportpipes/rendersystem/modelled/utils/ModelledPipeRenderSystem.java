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
import de.robotricker.transportpipes.protocol.DuctManager;
import de.robotricker.transportpipes.protocol.DuctProtocol;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeCOLOREDModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeEXTRACTIONModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeGOLDENModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeICEModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeIRONModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeModel;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeVOIDModel;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.staticutils.ProtocolUtils;

public class ModelledPipeRenderSystem extends RenderSystem {

	private Map<Pipe, ArmorStandData> midAsd = new HashMap<>();
	private Map<Pipe, Map<WrappedDirection, ArmorStandData>> connsAsd = new HashMap<>();
	private AxisAlignedBB midAABB;
	private Map<WrappedDirection, AxisAlignedBB> connsAABBs = new HashMap<>();

	private Map<PipeType, ModelledPipeModel> models = new HashMap<>();

	public ModelledPipeRenderSystem(DuctManager ductManager) {
		super(ductManager);
		models.put(PipeType.COLORED, new ModelledPipeCOLOREDModel());
		models.put(PipeType.ICE, new ModelledPipeICEModel());
		models.put(PipeType.GOLDEN, new ModelledPipeGOLDENModel());
		models.put(PipeType.IRON, new ModelledPipeIRONModel());
		models.put(PipeType.VOID, new ModelledPipeVOIDModel());
		models.put(PipeType.EXTRACTION, new ModelledPipeEXTRACTIONModel());

		midAABB = new AxisAlignedBB(4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d);
		connsAABBs.put(WrappedDirection.NORTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 0d / 16d, 12d / 16d, 12d / 16d, 4d / 16d));
		connsAABBs.put(WrappedDirection.EAST, new AxisAlignedBB(12d / 16d, 4d / 16d, 4d / 16d, 16d / 16d, 12d / 16d, 12d / 16d));
		connsAABBs.put(WrappedDirection.SOUTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d, 16d / 16d));
		connsAABBs.put(WrappedDirection.WEST, new AxisAlignedBB(0d / 16d, 4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d));
		connsAABBs.put(WrappedDirection.UP, new AxisAlignedBB(4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d, 16d / 16d, 12d / 16d));
		connsAABBs.put(WrappedDirection.DOWN, new AxisAlignedBB(4d / 16d, 0d / 16d, 4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d));
	}

	@Override
	public void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections) {
		if (midAsd.containsKey(duct)) {
			return;
		}
		Pipe pipe = (Pipe) duct;

		ModelledPipeModel model = models.get(pipe.getPipeType());
		midAsd.put(pipe, model.createMidASD(ModelledPipeMidModelData.createModelData(pipe)));
		Map<WrappedDirection, ArmorStandData> connsMap = new HashMap<>();
		connsAsd.put(pipe, connsMap);
		for (WrappedDirection conn : allConnections) {
			connsMap.put(conn, model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, conn)));
		}
	}

	@Override
	public void updateDuctASD(Duct duct) {
		Pipe pipe = (Pipe) duct;

		if (!midAsd.containsKey(pipe) || !connsAsd.containsKey(pipe) || connsAsd.get(pipe) == null) {
			return;
		}

		List<ArmorStandData> removedASD = new ArrayList<>();
		List<ArmorStandData> addedASD = new ArrayList<>();

		Map<WrappedDirection, ArmorStandData> connsMap = connsAsd.get(pipe);
		ModelledPipeModel model = models.get(pipe.getPipeType());

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
		List<Player> players = ductManager.getAllPlayersWithRenderSystem(this);
		int[] removedIds = ProtocolUtils.convertArmorStandListToEntityIdArray(removedASD);
		for (Player p : players) {
			ductManager.getProtocol().removeArmorStandDatas(p, removedIds);
			ductManager.getProtocol().sendArmorStandDatas(p, pipe.getBlockLoc(), addedASD);
		}

	}

	@Override
	public void destroyDuctASD(Duct duct) {
		Pipe pipe = (Pipe) duct;

		if (!midAsd.containsKey(pipe) || !connsAsd.containsKey(pipe) || connsAsd.get(pipe) == null) {
			return;
		}
		midAsd.remove(pipe);
		connsAsd.remove(pipe);
	}

	@Override
	public List<ArmorStandData> getASDForDuct(Duct duct) {
		Pipe pipe = (Pipe) duct;

		List<ArmorStandData> ASD = new ArrayList<>();
		if (midAsd.containsKey(pipe)) {
			ASD.add(midAsd.get(pipe));
		}
		if (connsAsd.containsKey(pipe)) {
			ASD.addAll(connsAsd.get(pipe).values());
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
		WrappedDirection clickedMidFace = midAABB.rayIntersection(ray, origin, pipe.getBlockLoc());
		if (clickedMidFace != null && !pipeConns.contains(clickedMidFace)) {
			return clickedMidFace;
		} else {
			double nearestDistanceSquared = Double.MAX_VALUE;
			WrappedDirection currentClickedConnFace = null;
			for (WrappedDirection pd : pipeConns) {
				AxisAlignedBB connAABB = connsAABBs.get(pd);
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
		aabbs.add(midAABB);
		Collection<WrappedDirection> pipeConns = pipe.getAllConnections();
		for (WrappedDirection pd : pipeConns) {
			aabbs.add(connsAABBs.get(pd));
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
	public boolean usesResourcePack() {
		return true;
	}
	
}
