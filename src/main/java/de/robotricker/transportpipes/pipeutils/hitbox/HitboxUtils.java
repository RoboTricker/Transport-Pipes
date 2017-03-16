package de.robotricker.transportpipes.pipeutils.hitbox;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Comparator;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.Lever;
import org.bukkit.material.TrapDoor;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.GoldenPipe;
import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeUtils;

public class HitboxUtils {

	public static final int HITBOX_RANGE = 5;
	private static Set<Material> lineOfSightSet = null;

	public static List<Block> getLineOfSight(Player p) {
		if (lineOfSightSet == null) {
			lineOfSightSet = new HashSet<Material>();
			lineOfSightSet.add(Material.WATER);
			lineOfSightSet.add(Material.STATIONARY_WATER);
			lineOfSightSet.add(Material.LAVA);
			lineOfSightSet.add(Material.STATIONARY_LAVA);
			lineOfSightSet.add(Material.AIR);
			//add transprant blocks, so that when you look only a little above the hitbox of e.g. a grass,
			//you will neverless click on the pipe unless you really click on the hitbox of the grass.
			//without this code, the line of sight will stop at this transparent block and won't recognize the pipe behind it.
			for (Material m : Material.values()) {
				if (m.isTransparent() && !lineOfSightSet.contains(m)) {
					lineOfSightSet.add(m);
				}
			}
		}
		return p.getLineOfSight(lineOfSightSet, HITBOX_RANGE);
	}

	/**
	 * returns the BlockFace of the pipe this player is looking to. Only invoke this if you made sure that this player is looking on this pipe.
	 */
	public static BlockFace getBlockFaceOfPipeLookingTo(Player p, Pipe pipe) {
		return pipe.getAABB().intersectRay(p.getEyeLocation().getDirection(), p.getEyeLocation(), pipe.blockLoc.getBlockX(), pipe.blockLoc.getBlockY(), pipe.blockLoc.getBlockZ());
	}

	public static Block getPipeLookingTo(Player p, Block clickedBlock) {

		List<Block> line = getLineOfSight(p);

		Block currentBlock = null;
		int indexOfPipeBlock = -1;
		int indexOfClickedBlock = -1;
		int i = 0;
		while (currentBlock == null || PipeUtils.getPipeWithLocation(currentBlock.getLocation()).getAABB().intersectRay(p.getEyeLocation().getDirection(), p.getEyeLocation(), currentBlock.getLocation().getBlockX(), currentBlock.getLocation().getBlockY(), currentBlock.getLocation().getBlockZ()) == null) {

			if (line.size() > i) {
				//check if on this block is a pipe
				if (PipeUtils.getPipeWithLocation(line.get(i).getLocation()) != null) {
					currentBlock = line.get(i);
					indexOfPipeBlock = i;
				}
				//check if the player looks on the hitbox of the pipe (the player could possibly look on a block with a pipe but not on the hitbox itself)
				if (currentBlock != null && PipeUtils.getPipeWithLocation(currentBlock.getLocation()).getAABB().intersectRay(p.getEyeLocation().getDirection(), p.getEyeLocation(), currentBlock.getLocation().getBlockX(), currentBlock.getLocation().getBlockY(), currentBlock.getLocation().getBlockZ()) == null) {
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
		BlockFace bf = PipeUtils.getPipeWithLocation(pipeLoc.getLocation()).getAABB().intersectRay(p.getEyeLocation().getDirection(), p.getEyeLocation(), pipeLoc.getLocation().getBlockX(), pipeLoc.getLocation().getBlockY(), pipeLoc.getLocation().getBlockZ());
		return pipeLoc.getRelative(bf);
	}

	public static void decreaseItemInHand(Player p, boolean mainHand) {
		if (p.getGameMode() == GameMode.CREATIVE) {
			return;
		}
		if (mainHand) {
			ItemStack is = p.getInventory().getItemInMainHand();
			if (is.getAmount() > 1) {
				is.setAmount(is.getAmount() - 1);
			} else {
				is = null;
			}
			p.getInventory().setItemInMainHand(is);
		} else {
			ItemStack is = p.getInventory().getItemInOffHand();
			if (is.getAmount() > 1) {
				is.setAmount(is.getAmount() - 1);
			} else {
				is = null;
			}
			p.getInventory().setItemInOffHand(is);
		}
	}

	/**
	 * "simulate" a block place when you click on the side of a pipe
	 */
	public static boolean placeBlock(Player p, Block b, int id, byte data) {
		if (!TransportPipes.canBuild(p, b)) {
			return false;
		}
		//check if there is already a pipe at this position
		
		Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(b.getWorld());
		if(pipeMap != null){
			if(pipeMap.containsKey(TransportPipes.blockLocToLong(b.getLocation()))){
				return false;
			}
		}
		
		if (!(b.getType() == Material.AIR || b.isLiquid())) {
			return false;
		}
		b.setTypeIdAndData(id, data, true);
		
		if (PipeUtils.isIdInventoryHolder(id)) {
			PipeUtils.updatePipeNeighborBlockSync(b, true);
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

	/**
	 * returns the class of the pipe you can place with this item, or null if there is no pipe available for this item
	 */
	public static Class<? extends Pipe> getPipeWithPipePlaceableItem(ItemStack item) {
		if (item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			String displayName = item.getItemMeta().getDisplayName();
			if (displayName != null) {
				if (displayName.equals(TransportPipes.PIPE_NAME)) {
					return Pipe.class;
				}
				if (displayName.equals(TransportPipes.GOLDEN_PIPE_NAME)) {
					return GoldenPipe.class;
				}
				if (displayName.equals(TransportPipes.IRON_PIPE_NAME)) {
					return IronPipe.class;
				}
			}
		}
		return null;
	}

}
