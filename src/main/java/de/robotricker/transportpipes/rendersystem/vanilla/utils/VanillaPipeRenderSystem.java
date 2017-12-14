package de.robotricker.transportpipes.rendersystem.vanilla.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.ProtocolUtils;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeEWModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeMIDModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeNSModel;
import de.robotricker.transportpipes.rendersystem.vanilla.VanillaPipeUDModel;
import de.robotricker.transportpipes.utils.DuctItemUtils;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;

public class VanillaPipeRenderSystem extends RenderSystem {

	private Map<Pipe, List<ArmorStandData>> pipeAsd = new HashMap<>();

	public VanillaPipeRenderSystem(ArmorStandProtocol protocol) {
		super(protocol);
	}

	@Override
	public void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections) {
		if (pipeAsd.containsKey(duct)) {
			return;
		}
		Pipe pipe = (Pipe) duct;

		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), allConnections);

		pipeAsd.put(pipe, shape.getModel().createASD(VanillaPipeModelData.createModelData(pipe)));

	}

	@Override
	public void updateDuctASD(Duct duct) {
		if (!pipeAsd.containsKey(duct) || pipeAsd.get(duct) == null) {
			return;
		}
		Pipe pipe = (Pipe) duct;

		List<ArmorStandData> removedASD = new ArrayList<>();
		List<ArmorStandData> addedASD = new ArrayList<>();

		List<ArmorStandData> oldASD = pipeAsd.get(pipe);

		Collection<WrappedDirection> conns = pipe.getAllConnections();
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

		if (!pipeAsd.containsKey(pipe) || pipeAsd.get(pipe) == null) {
			return;
		}
		pipeAsd.remove(pipe);
	}

	@Override
	public List<ArmorStandData> getASDForDuct(Duct duct) {
		Pipe pipe = (Pipe) duct;

		return pipeAsd.get(pipe);
	}

	@Override
	public WrappedDirection getClickedDuctFace(Player player, Duct duct) {
		if (duct == null) {
			return null;
		}
		Pipe pipe = (Pipe) duct;

		Vector ray = player.getEyeLocation().getDirection();
		Vector origin = player.getEyeLocation().toVector();

		return VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), pipe.getAllConnections()).getModel().getAABB().rayIntersection(ray, origin, pipe.getBlockLoc());
	}

	@Override
	public AxisAlignedBB getOuterHitbox(Duct duct) {
		if (duct == null) {
			return null;
		}
		Pipe pipe = (Pipe) duct;

		return VanillaPipeShape.getPipeShapeFromConnections(pipe.getPipeType(), pipe.getAllConnections()).getModel().getAABB();
	}

	@Override
	public void initPlayer(Player p) {
		p.updateInventory();
	}

	@Override
	public int[] getRenderSystemIds() {
		return new int[] { 0 };
	}

	@Override
	public DuctType getDuctType() {
		return DuctType.PIPE;
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

		public static VanillaPipeShape getPipeShapeFromConnections(PipeType pipeType, Collection<WrappedDirection> conn) {
			WrappedDirection[] array = conn.toArray(new WrappedDirection[0]);
			if (pipeType == PipeType.GOLDEN || pipeType == PipeType.IRON || pipeType == PipeType.VOID) {
				return MID;
			}
			if (conn.size() == 1) {
				WrappedDirection pd = array[0];
				if (pd == WrappedDirection.NORTH || pd == WrappedDirection.SOUTH) {
					return NORTH_SOUTH;
				} else if (pd == WrappedDirection.UP || pd == WrappedDirection.DOWN) {
					return UP_DOWN;
				} else if (pd == WrappedDirection.EAST || pd == WrappedDirection.WEST) {
					return EAST_WEST;
				}
			} else if (conn.size() == 2) {
				WrappedDirection pd1 = array[0];
				WrappedDirection pd2 = array[1];
				if (pd1.getOpposite() == pd2) {
					if (pd1 == WrappedDirection.NORTH || pd1 == WrappedDirection.SOUTH) {
						return NORTH_SOUTH;
					} else if (pd1 == WrappedDirection.UP || pd1 == WrappedDirection.DOWN) {
						return UP_DOWN;
					} else if (pd1 == WrappedDirection.EAST || pd1 == WrappedDirection.WEST) {
						return EAST_WEST;
					}
				}
			}
			return MID;
		}
	}

}
