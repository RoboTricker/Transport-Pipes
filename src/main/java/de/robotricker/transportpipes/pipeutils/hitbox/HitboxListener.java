package de.robotricker.transportpipes.pipeutils.hitbox;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.ClickablePipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class HitboxListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block clickedBlock = e.getClickedBlock();
		ItemStack clickedItem;
		boolean mainHand;

		if (e.getHand() == EquipmentSlot.HAND) {
			clickedItem = e.getPlayer().getEquipment().getItemInMainHand();
			mainHand = true;
		} else if (e.getHand() == EquipmentSlot.OFF_HAND) {
			if (!HitboxUtils.isInteractableItem(p.getInventory().getItemInMainHand()) && PipeType.getFromPipeItem(p.getInventory().getItemInMainHand()) == null) {
				clickedItem = e.getPlayer().getEquipment().getItemInOffHand();
				mainHand = false;
			} else {
				//using mainhand -> ignore/cancel the offhand call
				if (clickedBlock != null && HitboxUtils.isInteractableItem(p.getInventory().getItemInOffHand())) {
					Block placeBlock = clickedBlock.getRelative(e.getBlockFace());
					//cancel block placement if the block would be placed inside a pipe or the clickedItem in the mainHand is a pipe
					if (PipeUtils.getPipeWithLocation(placeBlock.getLocation()) != null || PipeType.getFromPipeItem(p.getInventory().getItemInMainHand()) != null) {
						e.setCancelled(true);
					}
				}
				return;
			}
		} else {
			return;
		}

		PipeType placeablePipeType = PipeType.getFromPipeItem(clickedItem);

		//left click on pipe (its irrelevant if you are looking on a block below the pipe or not)
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			final Block pipeBlock = HitboxUtils.getPipeLookingTo(p, clickedBlock);
			//****************************** LEFT CLICKED ON PIPE *******************************************
			if (pipeBlock != null) {
				e.setCancelled(true);
				if (PipeUtils.canBuild(p, pipeBlock, pipeBlock, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
					PipeUtils.destroyPipe(e.getPlayer(), PipeUtils.getPipeWithLocation(pipeBlock.getLocation()));
				}
			}

			//right click on pipe or a block (its irrelevant if you are looking on a block below the pipe or not)
		} else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			Block pipeBlock = HitboxUtils.getPipeLookingTo(p, clickedBlock);

			if (clickedItem.getType().isBlock()) {
				//****************************** PLACE BLOCK ON SIDE OF PIPE *******************************************
				if (pipeBlock != null) {
					e.setCancelled(true);
					Block placeBlock = HitboxUtils.getRelativeBlockOfPipe(p, pipeBlock);
					//cancel block placement if the player clicked at the pipe with a wrench
					if (!PipeItemUtils.isItemStackWrench(clickedItem)) {
						if (HitboxUtils.placeBlock(p, placeBlock, pipeBlock, clickedItem.getTypeId(), clickedItem.getData().getData(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
							HitboxUtils.decreaseItemInHand(p, mainHand);
							return;
						}
					}
					//player is looking on a block but not on a pipe hitbox (check if the block can be placed there)
					//****************************** CANCEL BLOCK PLACEMENT INSIDE PIPE *******************************************
				} else if (clickedBlock != null) {
					Block placeBlock = clickedBlock.getRelative(e.getBlockFace());
					//cancel block placement if the block would be placed inside a pipe
					if (PipeUtils.getPipeWithLocation(placeBlock.getLocation()) != null) {
						//only cancel the interaction if the player wants to place a block inside the pipe (if he looks onto an interactive block he has to sneak)
						if ((clickedItem.getType() != Material.AIR && (!HitboxUtils.isInteractiveBlock(clickedBlock) || p.isSneaking()))) {
							e.setCancelled(true);
							return;
						}
					}
				}
			}
			//place pipe
			if (placeablePipeType != null) {
				//clicked on pipe
				//****************************** PLACE PIPE ON SIDE OF PIPE *******************************************
				if (pipeBlock != null) {
					e.setCancelled(true);
					Block placeBlock = HitboxUtils.getRelativeBlockOfPipe(p, pipeBlock);
					if (PipeUtils.canBuild(p, placeBlock, pipeBlock, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
						if (PipeUtils.buildPipe(e.getPlayer(), placeBlock.getLocation(), placeablePipeType, PipeColor.getPipeColorByPipeItem(clickedItem))) {
							HitboxUtils.decreaseItemInHand(p, mainHand);
							return;
						}
					}
					//clicked on block (not below pipe)
					//****************************** PLACE PIPE ON RELATIVE OF BLOCK *******************************************
				} else if (clickedBlock != null) {
					Block placeBlock = clickedBlock.getRelative(e.getBlockFace());
					boolean canPlace = true;
					if (HitboxUtils.isInteractiveBlock(clickedBlock)) {
						canPlace = p.isSneaking();
					}
					if (canPlace) {
						if (PipeUtils.canBuild(p, placeBlock, clickedBlock, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
							if (PipeUtils.buildPipe(e.getPlayer(), placeBlock.getLocation(), placeablePipeType, PipeColor.getPipeColorByPipeItem(clickedItem))) {
								HitboxUtils.decreaseItemInHand(p, mainHand);
								e.setCancelled(true);
								return;
							}
						}
					}
				}
			}

			if (pipeBlock != null) {
				Pipe pipeClickedAt = PipeUtils.getPipeWithLocation(pipeBlock.getLocation());
				if (pipeClickedAt instanceof ClickablePipe) {
					if (PipeItemUtils.isItemStackWrench(clickedItem)) {
						if (PipeUtils.canBuild(p, pipeClickedAt.blockLoc.getBlock(), pipeClickedAt.blockLoc.getBlock(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
							((ClickablePipe) pipeClickedAt).click(p, HitboxUtils.getFaceOfPipeLookingTo(p, pipeClickedAt));
							e.setCancelled(true);
						}
					}
				}
			}

		}

	}

}
