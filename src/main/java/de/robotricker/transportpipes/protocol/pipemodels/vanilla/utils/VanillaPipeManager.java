package de.robotricker.transportpipes.protocol.pipemodels.vanilla.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
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
	public void createPipeASD(Pipe pipe, List<PipeDirection> allConnections) {
		if (pipeAsd.containsKey(pipe)) {
			return;
		}

		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), allConnections);

		pipeAsd.put(pipe, shape.getModel().createASD(VanillaPipeModelData.createModelData(pipe)));

	}

	@Override
	public void updatePipeASD(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe) || pipeAsd.get(pipe) == null) {
			return;
		}

		List<ArmorStandData> removedASD = new ArrayList<ArmorStandData>();
		List<ArmorStandData> addedASD = new ArrayList<ArmorStandData>();

		List<ArmorStandData> oldASD = pipeAsd.get(pipe);

		List<PipeDirection> conns = pipe.getAllConnections();
		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), conns);

		List<ArmorStandData> newASD = shape.getModel().createASD(VanillaPipeModelData.createModelData(pipe));

		List<ArmorStandData> tempASD = new ArrayList<ArmorStandData>();

		for (int i = 0; i < Math.max(oldASD.size(), newASD.size()); i++) {
			ArmorStandData ASDOld = i < oldASD.size() ? oldASD.get(i) : null;
			ArmorStandData ASDNew = i < newASD.size() ? newASD.get(i) : null;
			if (!((ASDOld != null && ASDOld.isSimilar(ASDNew)) || ASDOld == ASDNew)) {
				if (ASDOld != null) {
					removedASD.add(ASDOld);
				}
				if (ASDNew != null) {
					addedASD.add(ASDNew);
					tempASD.add(ASDNew);
				}
				continue;
			}
			if (ASDOld != null) {
				tempASD.add(ASDOld);
			}
		}

		oldASD.clear();
		oldASD.addAll(tempASD);

		//SEND TO CLIENTS
		List<Player> players = protocol.getPlayersWithPipeManager(this);
		int[] removedIds = TransportPipes.instance.convertArmorStandListToEntityIdArray(removedASD);
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
			protocol.sendArmorStandDatas(p, pipe.getBlockLoc(), addedASD);
		}

	}

	@Override
	public void destroyPipeASD(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe) || pipeAsd.get(pipe) == null) {
			return;
		}
		pipeAsd.remove(pipe);
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

		return VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), pipe.getAllConnections()).getModel().getAABB().rayIntersection(ray, origin, pipe.getBlockLoc());
	}

	@Override
	public void initPlayer(Player p) {

	}

	@Override
	public ItemStack getPipeItem(PipeType pipeType, PipeColor pipeColor) {
		switch (pipeType) {
		case COLORED:
			switch (pipeColor) {
			case WHITE:
				return VanillaPipeModel.ITEM_PIPE_WHITE;
			case BLUE:
				return VanillaPipeModel.ITEM_PIPE_BLUE;
			case RED:
				return VanillaPipeModel.ITEM_PIPE_RED;
			case YELLOW:
				return VanillaPipeModel.ITEM_PIPE_YELLOW;
			case GREEN:
				return VanillaPipeModel.ITEM_PIPE_GREEN;
			case BLACK:
				return VanillaPipeModel.ITEM_PIPE_BLACK;
			default:
				return null;
			}
		case GOLDEN:
			return VanillaPipeModel.ITEM_PIPE_GOLDEN;
		case IRON:
			return VanillaPipeModel.ITEM_PIPE_IRON;
		case ICE:
			return VanillaPipeModel.ITEM_PIPE_ICE;
		default:
			return null;
		}
	}

	@Override
	public ItemStack getWrenchItem() {
		return VanillaPipeModel.ITEM_WRENCH;
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

		public static VanillaPipeShape getPipeShapeFromConnections(PipeType pipeType, List<PipeDirection> conn) {
			if (pipeType == PipeType.GOLDEN || pipeType == PipeType.IRON) {
				return MID;
			}
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
