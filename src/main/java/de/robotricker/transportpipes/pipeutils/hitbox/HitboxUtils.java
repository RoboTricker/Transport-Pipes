package de.robotricker.transportpipes.pipeutils.hitbox;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Comparator;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.Lever;
import org.bukkit.material.TrapDoor;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeNeighborBlockUtils;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;

public class HitboxUtils {

	public final static int HITBOX_RANGE = 5;
	private final static Set<Material> LINE_OF_SIGHT_SET;

	static {
		LINE_OF_SIGHT_SET = new HashSet<Material>();
		LINE_OF_SIGHT_SET.add(Material.WATER);
		LINE_OF_SIGHT_SET.add(Material.STATIONARY_WATER);
		LINE_OF_SIGHT_SET.add(Material.LAVA);
		LINE_OF_SIGHT_SET.add(Material.STATIONARY_LAVA);
		LINE_OF_SIGHT_SET.add(Material.AIR);
		//add transprant blocks, so that when you look only a little above the hitbox of e.g. a grass,
		//you will neverless click on the pipe unless you really click on the hitbox of the grass.
		//without this code, the line of sight will stop at this transparent block and won't recognize the pipe behind it.
		for (Material m : Material.values()) {
			if (m.isTransparent()) {
				LINE_OF_SIGHT_SET.add(m);
			}
		}
	}

	public static List<Block> getLineOfSight(Player p) {
		return p.getLineOfSight(LINE_OF_SIGHT_SET, HITBOX_RANGE);
	}

	public static PipeDirection getFaceOfPipeLookingTo(Player p, Pipe pipe) {
		return TransportPipes.armorStandProtocol.getPlayerPipeManager(p).getClickedPipeFace(p, pipe);
	}

	public static Block getPipeLookingTo(Player p, Block clickedBlock) {

		List<Block> line = getLineOfSight(p);

		Block currentBlock = null;
		int indexOfPipeBlock = -1;
		int indexOfClickedBlock = -1;
		int i = 0;

		PipeManager playerPipeManager = TransportPipes.armorStandProtocol.getPlayerPipeManager(p);

		while (currentBlock == null) {

			if (line.size() > i) {
				//check if on this block is a pipe
				if (PipeUtils.getPipeWithLocation(line.get(i).getLocation()) != null) {
					currentBlock = line.get(i);
					indexOfPipeBlock = i;
				}
				//check if the player looks on the hitbox of the pipe (the player could possibly look on a block with a pipe but not on the hitbox itself)
				if (currentBlock != null && playerPipeManager.getClickedPipeFace(p, PipeUtils.getPipeWithLocation(currentBlock.getLocation())) == null) {
					currentBlock = null;
					indexOfPipeBlock = -1;
				}

			} else {
				break;
			}

			i++;

		}

		//calculate the index of the block clicked on
		if (clickedBlock != null) {
			if (line.contains(clickedBlock)) {
				indexOfClickedBlock = line.indexOf(clickedBlock);
			}
		}
		//check if the clicked block is before the "pipe block", so that you can't interact with a pipe behind a block
		if (indexOfPipeBlock != -1 && indexOfClickedBlock != -1) {
			if (indexOfClickedBlock <= indexOfPipeBlock) {
				return null;
			}
		}
		return currentBlock;
	}

	/**
	 * gets the neighbor block of the pipe (calculated by the player direction ray and the pipe hitbox)
	 */
	public static Block getRelativeBlockOfPipe(Player p, Block pipeLoc) {
		PipeDirection pd = TransportPipes.armorStandProtocol.getPlayerPipeManager(p).getClickedPipeFace(p, PipeUtils.getPipeWithLocation(pipeLoc.getLocation()));
		return pd != null ? pipeLoc.getRelative(pd.toBlockFace()) : null;
	}

	public static void decreaseItemInHand(Player p, boolean mainHand) {
		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}
		if (mainHand) {
			int amountBefore = p.getInventory().getItemInMainHand().getAmount();
			if (amountBefore - 1 == 0) {
				p.getInventory().setItemInMainHand(null);
			} else {
				ItemStack is = p.getInventory().getItemInMainHand();
				is.setAmount(amountBefore - 1);
				p.getInventory().setItemInMainHand(is);
			}
		} else {
			int amountBefore = p.getInventory().getItemInOffHand().getAmount();
			if (amountBefore - 1 == 0) {
				p.getInventory().setItemInOffHand(null);
			} else {
				ItemStack is = p.getInventory().getItemInOffHand();
				is.setAmount(amountBefore - 1);
				p.getInventory().setItemInOffHand(is);
			}
		}
	}

	/**
	 * "simulate" a block place when you click on the side of a pipe
	 */
	public static boolean placeBlock(Player p, Block b, Block placedAgainst, int id, byte data, EquipmentSlot es) {
		if (!TransportPipes.canBuild(p, b, placedAgainst, es)) {
			return false;
		}
		//check if there is already a pipe at this position

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(b.getWorld());
		if (pipeMap != null) {
			if (pipeMap.containsKey(TransportPipes.convertBlockLoc(b.getLocation()))) {
				return false;
			}
		}

		if (!(b.getType() == Material.AIR || b.isLiquid())) {
			return false;
		}
		b.setTypeIdAndData(id, data, true);

		if (PipeNeighborBlockUtils.isIdInventoryHolder(id)) {
			PipeNeighborBlockUtils.updatePipeNeighborBlockSync(b, true);
		}

		return true;
	}

	/**
	 * checks if this block would give a reaction if you click on it without shifting, e.g. opening a chest or switching a lever
	 */
	public static boolean isInteractiveBlock(Block b) {
		if (b == null || b.getState() == null) {
			return false;
		}
		if (b.getType() == Material.WORKBENCH || b.getType() == Material.ENCHANTMENT_TABLE || b.getType() == Material.ANVIL || b.getType() == Material.BREWING_STAND || b.getState() instanceof InventoryHolder || b.getState() instanceof NoteBlock) {
			return true;
		}
		if (b.getState().getData() instanceof Button || b.getState().getData() instanceof Lever || b.getState().getData() instanceof Door || b.getState().getData() instanceof TrapDoor || b.getState().getData() instanceof Gate || b.getState().getData() instanceof Comparator) {
			if (b.getType() != Material.IRON_DOOR && b.getType() != Material.IRON_DOOR_BLOCK && b.getType() != Material.IRON_TRAPDOOR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if this item is interactable
	 */
	public static boolean isInteractableItem(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return false;
		}
		if (item.getType().isBlock()) {
			return true;
		}
		if (item.getType() == Material.REDSTONE || item.getType() == Material.WATER_BUCKET || item.getType() == Material.LAVA_BUCKET) {
			return true;
		}
		if (item.getType() == Material.MONSTER_EGG) {
			return true;
		}
		if (item.getType() == Material.EGG || item.getType() == Material.SNOW_BALL || item.getType() == Material.BOW || item.getType() == Material.ENDER_PEARL || item.getType() == Material.EYE_OF_ENDER || item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.EXP_BOTTLE || item.getType() == Material.FIREWORK_CHARGE) {
			return true;
		}
		if (item.getType().isEdible()) {
			return true;
		}
		return false;
	}

}
