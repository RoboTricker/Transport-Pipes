package de.robotricker.transportpipes.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.DuctService;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.utils.HitboxUtils;
import de.robotricker.transportpipes.ItemService;

public class DuctListener implements Listener {

    private final List<Material> interactables = Arrays.asList(
            Material.ACACIA_DOOR,
            Material.ACACIA_FENCE_GATE,
            Material.ANVIL,
            Material.BEACON,
            Material.BED,
            Material.BIRCH_DOOR,
            Material.BIRCH_FENCE_GATE,
            Material.BOAT,
            Material.BOAT_ACACIA,
            Material.BOAT_BIRCH,
            Material.BOAT_DARK_OAK,
            Material.BOAT_JUNGLE,
            Material.BOAT_SPRUCE,
            Material.BREWING_STAND,
            Material.COMMAND,
            Material.CHEST,
            Material.DARK_OAK_DOOR,
            Material.DARK_OAK_FENCE_GATE,
            Material.DAYLIGHT_DETECTOR,
            Material.DAYLIGHT_DETECTOR_INVERTED,
            Material.DISPENSER,
            Material.DROPPER,
            Material.ENCHANTMENT_TABLE,
            Material.ENDER_CHEST,
            Material.FENCE_GATE,
            Material.FURNACE,
            Material.HOPPER,
            Material.HOPPER_MINECART,
            Material.ITEM_FRAME,
            Material.JUNGLE_DOOR,
            Material.JUNGLE_FENCE_GATE,
            Material.LEVER,
            Material.MINECART,
            Material.NOTE_BLOCK,
            Material.POWERED_MINECART,
            Material.REDSTONE_COMPARATOR,
            Material.REDSTONE_COMPARATOR_OFF,
            Material.REDSTONE_COMPARATOR_ON,
            Material.STORAGE_MINECART,
            Material.TRAP_DOOR,
            Material.TRAPPED_CHEST,
            Material.WOOD_BUTTON,
            Material.WOOD_DOOR,
            Material.WOODEN_DOOR);

    //makes sure that "callInteraction" is called with the mainHand and with the offHand every single time
    private Map<Player, Interaction> interactions = new HashMap<>();

    private DuctService ductService;
    private ItemService itemService;

    @Inject
    public DuctListener(DuctService ductService, ItemService itemService, JavaPlugin plugin) {
        this.ductService = ductService;
        this.itemService = itemService;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateInteractSet, 0L, 1L);
    }

    private void updateInteractSet() {
        Iterator<Player> events = interactions.keySet().iterator();
        while (events.hasNext()) {
            Player p = events.next();
            if (interactions.get(p) != null)
                callInteraction(interactions.get(p));
            events.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        if (e.getAction() == Action.PHYSICAL) {
            return;
        }

        if (e.getHand() == EquipmentSlot.HAND) {
            Interaction offHandInteraction = new Interaction(p, EquipmentSlot.OFF_HAND, p.getInventory().getItemInOffHand(), clickedBlock, e.getBlockFace(), e.getAction());
            interactions.put(p, offHandInteraction);

            Interaction mainHandInteraction = new Interaction(p, EquipmentSlot.HAND, p.getInventory().getItemInMainHand(), clickedBlock, e.getBlockFace(), e.getAction());
            callInteraction(mainHandInteraction);
            if (mainHandInteraction.cancel) e.setCancelled(true);
            if (mainHandInteraction.denyBlockUse) e.setUseInteractedBlock(Event.Result.DENY);
            if (mainHandInteraction.successful) {
                interactions.put(p, null);
            }
        } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
            if (interactions.containsKey(p) && interactions.get(p) == null) {
                return;
            }
            if (interactions.containsKey(p)) {
                interactions.remove(p);
            } else {
                Interaction mainHandInteraction = new Interaction(p, EquipmentSlot.HAND, p.getInventory().getItemInMainHand(), clickedBlock, e.getBlockFace(), e.getAction());
                callInteraction(mainHandInteraction);
                if (mainHandInteraction.successful) {
                    return;
                }
            }
            Interaction offHandInteraction = new Interaction(p, EquipmentSlot.OFF_HAND, p.getInventory().getItemInOffHand(), clickedBlock, e.getBlockFace(), e.getAction());
            callInteraction(offHandInteraction);
            if (offHandInteraction.cancel) e.setCancelled(true);
            if (offHandInteraction.denyBlockUse) e.setUseInteractedBlock(Event.Result.DENY);
        }

    }

    private void callInteraction(Interaction interaction) {
        if (interaction.action == Action.RIGHT_CLICK_AIR || interaction.action == Action.RIGHT_CLICK_BLOCK) {
            if (interaction.item != null) {
                Duct clickedDuct = HitboxUtils.getDuctLookingTo(ductService, interaction.p, interaction.clickedBlock);
                DuctType itemDuctType = itemService.readDuctNBTTags(interaction.item);
                boolean manualPlaceable = itemDuctType != null || interaction.item.getType().isSolid();

                if (interaction.item.getType() == Material.STICK && clickedDuct != null) {
                    //TODO: open duct inv
                    interaction.cancel = true;
                    interaction.successful = true;
                    return;
                }

                Block placeBlock = null;
                if (clickedDuct != null) {
                    placeBlock = HitboxUtils.getRelativeBlockOfDuct(ductService, interaction.p, clickedDuct.getBlockLoc().toBlock(interaction.p.getWorld()));
                } else if (interaction.clickedBlock != null) {
                    placeBlock = interaction.clickedBlock.getRelative(interaction.blockFace);
                }
                if (placeBlock != null && (placeBlock.getType().isSolid() || ductService.getDuctAtLoc(placeBlock.getLocation()) != null)) {
                    placeBlock = null;
                }
                if (clickedDuct == null && interaction.clickedBlock != null && interactables.contains(interaction.clickedBlock.getType()) && !interaction.p.isSneaking()) {
                    return;
                } else if (placeBlock == null || (clickedDuct != null && !manualPlaceable)) {
                    if (!interaction.item.getType().isEdible()) {
                        interaction.denyBlockUse = true;
                    }
                    return;
                }

                if (manualPlaceable) {
                    if (itemDuctType != null) {
                        Duct itemDuct = itemDuctType.getBaseDuctType().createDuct(ductService, itemDuctType, new BlockLocation(placeBlock.getLocation()), placeBlock.getWorld(), placeBlock.getChunk());
                        ductService.createDuct(itemDuct);
                        decreaseHandItem(interaction.p, interaction.hand);
                        interaction.cancel = true;
                        interaction.successful = true;
                    } else if (clickedDuct != null) {
                        placeBlock.setTypeIdAndData(interaction.item.getTypeId(), interaction.item.getData().getData(), true);
                        decreaseHandItem(interaction.p, interaction.hand);
                        interaction.cancel = true;
                        interaction.successful = true;
                    }
                }

            }
        } else if (interaction.action == Action.LEFT_CLICK_AIR || interaction.action == Action.LEFT_CLICK_BLOCK) {
            Duct clickedDuct = HitboxUtils.getDuctLookingTo(ductService, interaction.p, interaction.clickedBlock);
            if (clickedDuct != null) {
                ductService.destroyDuct(clickedDuct);
                interaction.cancel = true;
                interaction.successful = true;
            }
        }
    }

    private void decreaseHandItem(Player p, EquipmentSlot hand) {
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack item = hand == EquipmentSlot.HAND ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();
        if (item != null) {
            if (item.getAmount() <= 1) {
                item = null;
            } else {
                item.setAmount(item.getAmount() - 1);
            }
            if (hand == EquipmentSlot.HAND) p.getInventory().setItemInMainHand(item);
            else p.getInventory().setItemInOffHand(item);
        }
    }

    private class Interaction {
        Player p;
        EquipmentSlot hand;
        ItemStack item;
        Block clickedBlock;
        BlockFace blockFace;
        Action action;
        boolean cancel;
        boolean denyBlockUse;
        boolean successful = false;

        Interaction(Player p, EquipmentSlot hand, ItemStack item, Block clickedBlock, BlockFace blockFace, Action action) {
            this.p = p;
            this.hand = hand;
            this.item = item;
            this.clickedBlock = clickedBlock;
            this.blockFace = blockFace;
            this.action = action;
        }
    }

}
