package de.robotricker.transportpipes.rendersystem.vanilla.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.ProtocolUtils;
import de.robotricker.transportpipes.rendersystem.PipeRenderSystem;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeEWModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeMIDModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeNSModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeUDModel;

public class VanillaPipeRenderSystem extends PipeRenderSystem {

	private Map<Pipe, List<ArmorStandData>> pipeAsd = new HashMap<>();

	public VanillaPipeRenderSystem(ArmorStandProtocol protocol) {
		super(protocol);
	}

	@Override
	public void createPipeASD(Pipe pipe, Collection<PipeDirection> allConnections) {
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

		List<ArmorStandData> removedASD = new ArrayList<>();
		List<ArmorStandData> addedASD = new ArrayList<>();

		List<ArmorStandData> oldASD = pipeAsd.get(pipe);

		Collection<PipeDirection> conns = pipe.getAllConnections();
		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), conns);

		List<ArmorStandData> newASD = shape.getModel().createASD(VanillaPipeModelData.createModelData(pipe));

		List<ArmorStandData> tempASD = new ArrayList<>();

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
		List<Player> players = protocol.getAllPlayersWithPipeManager(this);
		int[] removedIds = ProtocolUtils.convertArmorStandListToEntityIdArray(removedASD);
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
		return pipeAsd.get(pipe);
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
	public AxisAlignedBB getOuterHitbox(Pipe pipe) {
		if (pipe == null) {
			return null;
		}
		return VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), pipe.getAllConnections()).getModel().getAABB();
	}

	@Override
	public void initPlayer(Player p) {
		p.updateInventory();
	}

	@Override
	public String getPipeRenderSystemName() {
		return LocConf.load(LocConf.SETTINGS_RENDERSYSTEM_VANILLA);
	}

	@Override
	public ItemStack getRepresentationItem() {
		return PipeItemUtils.getPipeItem(PipeType.COLORED, PipeColor.WHITE);
	}

	@Override
	public int getRenderSystemId() {
		return 0;
	}

	private enum VanillaPipeShape {
		EAST_WEST(new VanillaPipeEWModel()),
		NORTH_SOUTH(new VanillaPipeNSModel()),
		UP_DOWN(new VanillaPipeUDModel()),
		MID(new VanillaPipeMIDModel());

		private VanillaPipeModel model;

		VanillaPipeShape(VanillaPipeModel model) {
			this.model = model;
		}

		public VanillaPipeModel getModel() {
			return model;
		}

		public static VanillaPipeShape getPipeShapeFromConnections(PipeType pipeType, Collection<PipeDirection> conn) {
			PipeDirection[] array = conn.toArray(new PipeDirection[0]);
			if (pipeType == PipeType.GOLDEN || pipeType == PipeType.IRON || pipeType == PipeType.VOID) {
				return MID;
			}
			if (conn.size() == 1) {
				PipeDirection pd = array[0];
				if (pd == PipeDirection.NORTH || pd == PipeDirection.SOUTH) {
					return NORTH_SOUTH;
				} else if (pd == PipeDirection.UP || pd == PipeDirection.DOWN) {
					return UP_DOWN;
				} else if (pd == PipeDirection.EAST || pd == PipeDirection.WEST) {
					return EAST_WEST;
				}
			} else if (conn.size() == 2) {
				PipeDirection pd1 = array[0];
				PipeDirection pd2 = array[1];
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
