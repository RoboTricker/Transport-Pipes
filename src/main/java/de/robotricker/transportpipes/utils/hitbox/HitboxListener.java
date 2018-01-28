package de.robotricker.transportpipes.utils.hitbox;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.ClickableDuct;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.DuctUtils;
import io.sentry.Sentry;

public class HitboxListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent e) {
		try {
			Player p = e.getPlayer();
			Block clickedBlock = e.getClickedBlock();
			ItemStack clickedItem;
			boolean mainHand;

			if (e.getHand() == EquipmentSlot.HAND) {
				clickedItem = e.getPlayer().getEquipment().getItemInMainHand();
				mainHand = true;
			} else if (e.getHand() == EquipmentSlot.OFF_HAND) {
				if (!HitboxUtils.isInteractableItem(p.getInventory().getItemInMainHand()) && DuctItemUtils.getDuctDetailsOfItem(p.getInventory().getItemInMainHand()) == null) {
					clickedItem = e.getPlayer().getEquipment().getItemInOffHand();
					mainHand = false;
				} else {
					// using mainhand -> ignore/cancel the offhand call
					if (clickedBlock != null && HitboxUtils.isInteractableItem(p.getInventory().getItemInOffHand())) {
						Block placeBlock = clickedBlock.getRelative(e.getBlockFace());
						// cancel block placement if the block would be placed inside a duct or the
						// clickedItem in the mainHand is a duct item
						if (DuctUtils.getDuctAtLocation(placeBlock.getLocation()) != null || DuctItemUtils.getDuctDetailsOfItem(p.getInventory().getItemInMainHand()) != null) {
							e.setCancelled(true);
						}
					}
					return;
				}
			} else {
				return;
			}

			DuctDetails ductDetails = DuctItemUtils.getDuctDetailsOfItem(clickedItem);

			// left click on duct (its irrelevant if you are looking on a block below the
			// duct or not)
			if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				final Block ductBlock = HitboxUtils.getDuctBlockLookingTo(p, clickedBlock);
				// ****************************** LEFT CLICKED ON DUCT
				// *******************************************
				if (ductBlock != null) {
					e.setCancelled(true);
					if (DuctUtils.canBuild(p, ductBlock, ductBlock, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
						DuctUtils.destroyDuct(p, DuctUtils.getDuctAtLocation(ductBlock.getLocation()), true);
					}
				}
				// right click on duct or a block (its irrelevant if you are looking on a block
				// below the duct or not)
			} else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block ductBlock = HitboxUtils.getDuctBlockLookingTo(p, clickedBlock);

				if (clickedItem.getType().isBlock() && clickedItem.getType() != Material.AIR) {
					// ****************************** PLACE BLOCK ON SIDE OF DUCT
					// *******************************************
					if (ductBlock != null) {
						e.setCancelled(true);
						Block placeBlock = HitboxUtils.getRelativeBlockOfDuct(p, ductBlock);
						// cancel block placement if the player clicked at the duct with a wrench
						if (HitboxUtils.placeBlock(p, placeBlock, ductBlock, clickedItem.getTypeId(), clickedItem.getData().getData(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
							HitboxUtils.decreaseItemInHand(p, mainHand);
							return;
						}
						// player is looking on a block but not on a duct hitbox (check if the block can
						// be placed there)
						// ****************************** CANCEL BLOCK PLACEMENT INSIDE DUCT
						// *******************************************
					} else if (clickedBlock != null) {
						Block placeBlock = clickedBlock.getRelative(e.getBlockFace());
						// cancel block placement if the block would be placed inside a duct
						if (DuctUtils.getDuctAtLocation(placeBlock.getLocation()) != null) {
							// only cancel the interaction if the player wants to place a block inside the
							// duct (if he looks onto an interactive block he has to sneak)
							if (!(HitboxUtils.isInteractiveBlock(clickedBlock) || p.isSneaking())) {
								e.setCancelled(true);
								return;
							}
						}
					}
				}
				// place duct
				if (ductDetails != null) {
					// clicked on duct
					// ****************************** PLACE PIPE ON SIDE OF DUCT
					// *******************************************
					if (ductBlock != null) {
						e.setCancelled(true);
						Block placeBlock = HitboxUtils.getRelativeBlockOfDuct(p, ductBlock);
						if (DuctUtils.canBuild(p, placeBlock, ductBlock, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
							if (DuctUtils.buildDuct(e.getPlayer(), placeBlock.getLocation(), ductDetails)) {
								HitboxUtils.decreaseItemInHand(p, mainHand);
								return;
							}
						}
						// clicked on block (not below duct)
						// ****************************** PLACE DUCT ON RELATIVE OF BLOCK
						// *******************************************
					} else if (clickedBlock != null) {
						e.setUseItemInHand(Result.DENY);
						Block placeBlock = clickedBlock.getRelative(e.getBlockFace());
						boolean canPlace = true;
						if (HitboxUtils.isInteractiveBlock(clickedBlock)) {
							canPlace = p.isSneaking();
						}
						if (canPlace) {
							if (DuctUtils.canBuild(p, placeBlock, clickedBlock, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
								if (DuctUtils.buildDuct(e.getPlayer(), placeBlock.getLocation(), ductDetails)) {
									HitboxUtils.decreaseItemInHand(p, mainHand);
									e.setCancelled(true);
									return;
								}
							}
						}
					}
				}

				if (ductBlock != null) {
					Duct ductClickedAt = DuctUtils.getDuctAtLocation(ductBlock.getLocation());
					if (ductClickedAt instanceof ClickableDuct) {
						if (DuctItemUtils.getWrenchItem().isSimilar(clickedItem)) {
							if (DuctUtils.canBuild(p, ductClickedAt.getBlockLoc().getBlock(), ductClickedAt.getBlockLoc().getBlock(), mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND)) {
								((ClickableDuct) ductClickedAt).click(p, HitboxUtils.getFaceOfDuctLookingTo(p, ductClickedAt));
								e.setCancelled(true);
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			Sentry.capture(ex);
		}
	}

}
