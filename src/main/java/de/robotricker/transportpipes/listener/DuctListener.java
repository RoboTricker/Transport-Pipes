package de.robotricker.transportpipes.listener;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.ThreadService;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
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
import org.bukkit.util.Vector;

import javax.inject.Inject;
import java.util.*;

public class DuctListener implements Listener {

    private final List<Material> interactables = new ArrayList<>();

    //makes sure that "callInteraction" is called with the mainHand and with the offHand every single time
    private Map<Player, Interaction> interactions = new HashMap<>();

    private ItemService itemService;
    private DuctRegister ductRegister;
    private GlobalDuctManager globalDuctManager;
    private TPContainerListener tpContainerListener;
    private GeneralConf generalConf;
    private SentryService sentry;
    private TransportPipes transportPipes;
    private ThreadService threadService;
    private PlayerSettingsService playerSettingsService;

    @Inject
    public DuctListener(ItemService itemService, JavaPlugin plugin, DuctRegister ductRegister, GlobalDuctManager globalDuctManager, TPContainerListener tpContainerListener, GeneralConf generalConf, SentryService sentry, TransportPipes transportPipes, ThreadService threadService, PlayerSettingsService playerSettingsService) {
        this.itemService = itemService;
        this.ductRegister = ductRegister;
        this.globalDuctManager = globalDuctManager;
        this.tpContainerListener = tpContainerListener;
        this.generalConf = generalConf;
        this.sentry = sentry;
        this.transportPipes = transportPipes;
        this.threadService = threadService;
        this.playerSettingsService = playerSettingsService;

        for (Material m : Material.values()) {
            if (m.isInteractable()) {
                interactables.add(m);
            }
        }

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
                e.setCancelled(true);
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

                // ********************** WRENCH DUCT CLICK ****************************
                if (clickedDuct != null && itemService.isWrench(interaction.item)) {
                    //wrench click
                    if (buildAllowed(interaction.p, clickedDuct.getBlockLoc().toBlock(interaction.p.getWorld()))) {
                        clickedDuct.notifyClick(interaction.p, interaction.p.isSneaking());
                    }

                    interaction.cancel = true;
                    interaction.successful = true;
                    return;
                }

                // ********************** WRENCH NON DUCT CLICK ****************************
                if (clickedDuct == null && itemService.isWrench(interaction.item) && interaction.p.isSneaking()) {
                    //wrench click

                    WorldUtils.startShowHiddenDuctsProcess(interaction.p, globalDuctManager, threadService, transportPipes, generalConf, playerSettingsService);

                    interaction.cancel = true;
                    interaction.successful = true;
                    return;
                }

                // ********************** DUCT OBFUSCATION ****************************
                if (clickedDuct != null && !interaction.p.isSneaking() && canBeUsedToObfuscate(interaction.item.getType())) {
                    // block can be used to obfuscate and player is not sneaking
                    // this block will be used to obfuscate the duct
                    Block ductBlock = clickedDuct.getBlockLoc().toBlock(interaction.p.getWorld());
                    if (buildAllowed(interaction.p, ductBlock)) {

                        BlockData bd = interaction.item.getType().createBlockData();
                        setDirectionalBlockFace(ductBlock.getLocation(), bd, interaction.p);
                        ductBlock.setBlockData(bd, true);
                        clickedDuct.obfuscatedWith(bd);

                        decreaseHandItem(interaction.p, interaction.hand);
                    }

                    interaction.cancel = true;
                    interaction.successful = true;
                    return;
                }

                // ********************** PREPARATIONS FOR DUCT / BLOCK PLACE ****************************
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

                // ********************** DUCT AND BLOCK PLACE ****************************
                if (manualPlaceable) {
                    if (itemDuctType != null) {

                        // duct placement
                        if (buildAllowed(interaction.p, placeBlock)) {
                            boolean lwcAllowed = true;
                            for (TPDirection dir : TPDirection.values()) {
                                if (WorldUtils.lwcProtection(placeBlock.getRelative(dir.getBlockFace()))) {
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
                                LangConf.Key.PROTECTED_BLOCK.sendMessage(interaction.p);
                            }
                        }

                        interaction.cancel = true;
                        interaction.successful = true;
                    } else if (clickedDuct != null) {
                        //block placement next to duct
                        if (buildAllowed(interaction.p, placeBlock)) {

                            BlockData bd = interaction.item.getType().createBlockData();
                            setDirectionalBlockFace(placeBlock.getLocation(), bd, interaction.p);
                            placeBlock.setBlockData(bd, true);

                            // create TransportPipesContainer from placed block if it is such
                            if (WorldUtils.isContainerBlock(interaction.item.getType())) {
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

    private boolean canBeUsedToObfuscate(Material type) {
        return type.isOccluding() && !type.isInteractable() && !type.hasGravity();
    }

    private void setDirectionalBlockFace(Location b, BlockData bd, Player p) {
        if (bd instanceof Directional) {
            Vector dir = new Vector(b.getX() + 0.5d, b.getY() + 0.5d, b.getZ() + 0.5d);
            dir.subtract(p.getEyeLocation().toVector());
            double absX = Math.abs(dir.getX());
            double absY = Math.abs(dir.getY());
            double absZ = Math.abs(dir.getZ());
            if (((Directional) bd).getFaces().contains(BlockFace.UP) && ((Directional) bd).getFaces().contains(BlockFace.DOWN)) {
                if (absX >= absY && absX >= absZ) {
                    ((Directional) bd).setFacing(dir.getX() > 0 ? BlockFace.WEST : BlockFace.EAST);
                } else if (absY >= absX && absY >= absZ) {
                    ((Directional) bd).setFacing(dir.getY() > 0 ? BlockFace.DOWN : BlockFace.UP);
                } else {
                    ((Directional) bd).setFacing(dir.getZ() > 0 ? BlockFace.NORTH : BlockFace.SOUTH);
                }
            } else {
                if (absX >= absZ) {
                    ((Directional) bd).setFacing(dir.getX() > 0 ? BlockFace.WEST : BlockFace.EAST);
                } else {
                    ((Directional) bd).setFacing(dir.getZ() > 0 ? BlockFace.NORTH : BlockFace.SOUTH);
                }
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


    private boolean buildAllowed(Player p, Block b) {
        if (generalConf.getDisabledWorlds().contains(b.getWorld().getName())) {
            return false;
        }
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
