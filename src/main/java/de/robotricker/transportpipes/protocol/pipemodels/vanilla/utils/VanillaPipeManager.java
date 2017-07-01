package de.robotricker.transportpipes.protocol.pipemodels.vanilla.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeEWModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeMIDModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeNSModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeUDModel;

public class VanillaPipeManager extends PipeManager {

	private Map<Pipe, List<ArmorStandData>> pipeAsd = new HashMap<Pipe, List<ArmorStandData>>();

	public VanillaPipeManager(ArmorStandProtocol protocol) {
		super(protocol);
	}

	@Override
	public void sendPipe(Pipe pipe) {
		if (pipeAsd.containsKey(pipe)) {
			return;
		}

		List<PipeDirection> conns = pipe.getAllConnections();
		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(conns);

		pipeAsd.put(pipe, shape.getModel().createASD(VanillaPipeModelData.createModelData(pipe)));

	}

	@Override
	public void updatePipe(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}

		List<ArmorStandData> removedASD = new ArrayList<ArmorStandData>();
		List<ArmorStandData> addedASD = new ArrayList<ArmorStandData>();

		List<ArmorStandData> oldASD = pipeAsd.get(pipe);

		List<PipeDirection> conns = pipe.getAllConnections();
		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(conns);

		List<ArmorStandData> newASD = shape.getModel().createASD(VanillaPipeModelData.createModelData(pipe));

		for (int i = 0; i < Math.max(oldASD.size(), newASD.size()); i++) {
			ArmorStandData ASD1 = i < oldASD.size() ? oldASD.get(i) : null;
			ArmorStandData ASD2 = i < newASD.size() ? newASD.get(i) : null;
			if (!((ASD1 != null && ASD1.isSimilar(ASD2)) || ASD1 == ASD2)) {
				if (ASD1 != null)
					removedASD.add(ASD1);
				if (ASD2 != null)
					addedASD.add(ASD2);
			}
		}

		oldASD.clear();
		oldASD.addAll(newASD);

		//SEND TO CLIENTS
		List<Player> players = protocol.getPlayersWithPipeManager(this);
		int[] removedIds = new int[removedASD.size()];
		for (int i = 0; i < removedIds.length; i++) {
			removedIds[i] = removedASD.get(i).getEntityID();
			if (removedIds[i] == -1) {
				System.err.println("ERRRRRROR: ______________________ -1");
			}
		}
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
			protocol.sendArmorStandDatas(p, pipe.getBlockLoc(), addedASD);
		}

	}

	@Override
	public void destroyPipe(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}
		List<ArmorStandData> ASD = pipeAsd.remove(pipe);

		//SEND TO CLIENTS
		List<Player> players = protocol.getPlayersWithPipeManager(this);
		int[] removedIds = new int[ASD.size()];
		for (int i = 0; i < removedIds.length; i++) {
			removedIds[i] = ASD.get(i).getEntityID();
			if (removedIds[i] == -1) {
				System.err.println("ERRRRRROR4444: ______________________ -1");
			}
		}
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
		}

	}

	@Override
	public List<ArmorStandData> getASDForPipe(Pipe pipe) {
		return pipeAsd.getOrDefault(pipe, null);
	}

	@Override
	public PipeDirection getClickedPipeFace(Player player, Pipe pipe) {

		if (pipe == null) {
			return null;
		}

		Vector ray = player.getEyeLocation().getDirection();
		Vector origin = player.getEyeLocation().toVector();

		return VanillaPipeShape.getPipeShapeFromConnections(pipe.getAllConnections()).getModel().getAABB().rayIntersection(ray, origin, pipe.getBlockLoc());
	}

	private enum VanillaPipeShape {
		EAST_WEST(new VanillaPipeEWModel()),
		NORTH_SOUTH(new VanillaPipeNSModel()),
		UP_DOWN(new VanillaPipeUDModel()),
		MID(new VanillaPipeMIDModel());

		private VanillaPipeModel model;

		private VanillaPipeShape(VanillaPipeModel model) {
			this.model = model;
		}

		public VanillaPipeModel getModel() {
			return model;
		}

		public static VanillaPipeShape getPipeShapeFromConnections(List<PipeDirection> conn) {
			if (conn.size() == 1) {
				PipeDirection pd = conn.get(0);
				if (pd == PipeDirection.NORTH || pd == PipeDirection.SOUTH) {
					return NORTH_SOUTH;
				} else if (pd == PipeDirection.UP || pd == PipeDirection.DOWN) {
					return UP_DOWN;
				} else if (pd == PipeDirection.EAST || pd == PipeDirection.WEST) {
					return EAST_WEST;
				}
			} else if (conn.size() == 2) {
				PipeDirection pd1 = conn.get(0);
				PipeDirection pd2 = conn.get(1);
				if (pd1.getOpposite() == pd2) {
					if (pd1 == PipeDirection.NORTH || pd1 == PipeDirection.SOUTH) {
						return NORTH_SOUTH;
					} else if (pd1 == PipeDirection.UP || pd1 == PipeDirection.DOWN) {
						return UP_DOWN;
					} else if (pd1 == PipeDirection.EAST || pd1 == PipeDirection.WEST) {
						return EAST_WEST;
					}
				}
			}
			return MID;
		}
	}

}
