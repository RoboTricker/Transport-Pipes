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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.log.SentryService;
import de.robotricker.transportpipes.utils.HitboxUtils;
import de.robotricker.transportpipes.utils.WorldUtils;
import io.sentry.Sentry;

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

    private ItemService itemService;
    private DuctRegister ductRegister;
    private GlobalDuctManager globalDuctManager;
    private TPContainerListener tpContainerListener;
    private GeneralConf generalConf;
    private SentryService sentry;

    @Inject
    public DuctListener(ItemService itemService, JavaPlugin plugin, DuctRegister ductRegister, GlobalDuctManager globalDuctManager, TPContainerListener tpContainerListener, GeneralConf generalConf, SentryService sentry) {
        this.itemService = itemService;
        this.ductRegister = ductRegister;
        this.globalDuctManager = globalDuctManager;
        this.tpContainerListener = tpContainerListener;
        this.generalConf = generalConf;
        this.sentry = sentry;
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
                Duct clickedDuct = HitboxUtils.getDuctLookingTo(globalDuctManager, interaction.p, interaction.clickedBlock);
                DuctType itemDuctType = itemService.readDuctNBTTags(interaction.item, ductRegister);
                boolean manualPlaceable = itemDuctType != null || interaction.item.getType().isSolid();

                if (interaction.item.isSimilar(itemService.getWrench()) && clickedDuct != null) {

                    //wrench click
                    clickedDuct.notifyClick(interaction.p, TPDirection.fromBlockFace(interaction.blockFace), interaction.p.isSneaking());

                    interaction.cancel = true;
                    interaction.successful = true;
                    return;
                }

                Block placeBlock = null;
                if (clickedDuct != null) {
                    placeBlock = HitboxUtils.getRelativeBlockOfDuct(globalDuctManager, interaction.p, clickedDuct.getBlockLoc().toBlock(interaction.p.getWorld()));
                } else if (interaction.clickedBlock != null) {
                    placeBlock = interaction.clickedBlock.getRelative(interaction.blockFace);
                }
                if (placeBlock != null && (placeBlock.getType().isSolid() || globalDuctManager.getDuctAtLoc(placeBlock.getLocation()) != null)) {
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

                        // duct placement
                        if (buildAllowed(interaction.p, placeBlock)) {
                            boolean lwcAllowed = true;
                            for (TPDirection dir : TPDirection.values()) {
                                if (lwcProtection(placeBlock.getRelative(dir.getBlockFace()))) {
                                    lwcAllowed = false;
                                }
                            }
                            if (lwcAllowed) {
                                Duct duct = globalDuctManager.createDuctObject(itemDuctType, new BlockLocation(placeBlock.getLocation()), placeBlock.getWorld(), placeBlock.getChunk());
                                globalDuctManager.registerDuct(duct);
                                globalDuctManager.updateDuctConnections(duct);
                                globalDuctManager.registerDuctInRenderSystems(duct, true);
                                globalDuctManager.updateNeighborDuctsConnections(duct);
                                globalDuctManager.updateNeighborDuctsInRenderSystems(duct, true);

                                decreaseHandItem(interaction.p, interaction.hand);
                            } else {
                                interaction.p.sendMessage("§cYou cannot place a pipe next to a protected block");
                            }
                        }

                        interaction.cancel = true;
                        interaction.successful = true;
                    } else if (clickedDuct != null) {
                        //block placement
                        if (buildAllowed(interaction.p, placeBlock)) {
                            placeBlock.setTypeIdAndData(interaction.item.getTypeId(), interaction.item.getData().getData(), true);
                            // create TPContainer from placed block if it is such
                            if (WorldUtils.isIdContainerBlock(interaction.item.getTypeId())) {
                                tpContainerListener.updateContainerBlock(placeBlock, true, true);
                            }
                            decreaseHandItem(interaction.p, interaction.hand);
                        }
                        interaction.cancel = true;
                        interaction.successful = true;
                    }
                }

            }
        } else if (interaction.action == Action.LEFT_CLICK_AIR || interaction.action == Action.LEFT_CLICK_BLOCK) {
            Duct clickedDuct = HitboxUtils.getDuctLookingTo(globalDuctManager, interaction.p, interaction.clickedBlock);
            // duct destruction
            if (clickedDuct != null) {
                if (buildAllowed(interaction.p, clickedDuct.getBlockLoc().toBlock(interaction.p.getWorld()))) {
                    globalDuctManager.unregisterDuct(clickedDuct);
                    globalDuctManager.unregisterDuctInRenderSystem(clickedDuct, true);
                    globalDuctManager.updateNeighborDuctsConnections(clickedDuct);
                    globalDuctManager.updateNeighborDuctsInRenderSystems(clickedDuct, true);
                    globalDuctManager.playDuctDestroyActions(clickedDuct, interaction.p);
                }

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

    private boolean lwcProtection(Block b) {
        if (Bukkit.getPluginManager().isPluginEnabled("LWC")) {
            try {
                com.griefcraft.model.Protection protection = com.griefcraft.lwc.LWC.getInstance().findProtection(b);
                return protection != null && protection.getType() != com.griefcraft.model.Protection.Type.PUBLIC;
            } catch (Exception e) {
                e.printStackTrace();
                sentry.record(e);
            }
        }
        return false;
    }


    private boolean buildAllowed(Player p, Block b) {
        if (p.isOp()) {
            return true;
        }

        BlockBreakEvent event = new BlockBreakEvent(b, p);

        // unregister anticheat listeners
        List<RegisteredListener> unregisterListeners = new ArrayList<>();
        for (RegisteredListener rl : event.getHandlers().getRegisteredListeners()) {
            for (String antiCheat : generalConf.getAnticheatPlugins()) {
                if (rl.getPlugin().getName().equalsIgnoreCase(antiCheat)) {
                    unregisterListeners.add(rl);
                }
            }
            if (rl.getListener().equals(tpContainerListener)) {
                unregisterListeners.add(rl);
            }
        }
        for (RegisteredListener rl : unregisterListeners) {
            event.getHandlers().unregister(rl);
        }

        Bukkit.getPluginManager().callEvent(event);

        // register anticheat listeners
        event.getHandlers().registerAll(unregisterListeners);

        return !event.isCancelled();
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
