package de.robotricker.transportpipes.duct.manager;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.duct.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.duct.types.pipetype.PipeType;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.utils.Constants;
import de.robotricker.transportpipes.utils.WorldUtils;

public class PipeManager extends DuctManager<Pipe> {

    private static final long BIG_TICK_COUNT = 10;

    /**
     * ThreadSafe
     **/
    private Map<World, Map<BlockLocation, TPContainer>> containers;

    /**
     * THREAD-SAFE
     */
    private Map<Player, Set<PipeItem>> playerItems;

    private long tickCounter;

    @Inject
    public PipeManager(TransportPipes transportPipes, DuctRegister ductRegister, GlobalDuctManager globalDuctManager, ProtocolService protocolService, ItemService itemService) {
        super(transportPipes, ductRegister, globalDuctManager, protocolService, itemService);
        playerItems = Collections.synchronizedMap(new HashMap<>());
        containers = Collections.synchronizedMap(new HashMap<>());
        tickCounter = 0;
    }

    public Map<World, Map<BlockLocation, TPContainer>> getContainers() {
        return containers;
    }

    public Map<BlockLocation, TPContainer> getContainers(World world) {
        return containers.computeIfAbsent(world, v -> Collections.synchronizedMap(new TreeMap<>()));
    }

    public TPContainer getContainerAtLoc(World world, BlockLocation blockLoc) {
        Map<BlockLocation, TPContainer> containerMap = getContainers(world);
        return containerMap.get(blockLoc);
    }

    public TPContainer getContainerAtLoc(Location location) {
        return getContainerAtLoc(location.getWorld(), new BlockLocation(location));
    }

    @Override
    public void updateNonDuctConnections(Duct duct) {
        Pipe pipe = (Pipe) duct;
        pipe.getContainerConnections().clear();
        for (TPDirection tpDir : TPDirection.values()) {
            TPContainer neighborContainer = getContainerAtLoc(pipe.getWorld(), pipe.getBlockLoc().getNeighbor(tpDir));
            if (neighborContainer != null) {
                pipe.getContainerConnections().put(tpDir, neighborContainer);
            }
        }
    }

    @Override
    public void registerDuctTypes() {
        PipeType pipeType;
        BaseDuctType<Pipe> pipeBaseDuctType = ductRegister.baseDuctTypeOf("pipe");

        pipeType = new ColoredPipeType(pipeBaseDuctType, "White", LangConf.Key.COLORS_WHITE.get() + " " + LangConf.Key.PIPES_PIPE.get(), DyeColor.WHITE);
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Blue", LangConf.Key.COLORS_BLUE.get() + " " + LangConf.Key.PIPES_PIPE.get(), DyeColor.BLUE);
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Red", LangConf.Key.COLORS_RED.get() + " " + LangConf.Key.PIPES_PIPE.get(), DyeColor.RED);
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Yellow", LangConf.Key.COLORS_YELLOW.get() + " " + LangConf.Key.PIPES_PIPE.get(), DyeColor.YELLOW);
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Green", LangConf.Key.COLORS_GREEN.get() + " " + LangConf.Key.PIPES_PIPE.get(), DyeColor.GREEN);
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Black", LangConf.Key.COLORS_BLACK.get() + " " + LangConf.Key.PIPES_PIPE.get(), DyeColor.BLACK);
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Golden", LangConf.Key.PIPES_GOLDEN.get() + " " + LangConf.Key.PIPES_PIPE.get());
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Iron", LangConf.Key.PIPES_IRON.get() + " " + LangConf.Key.PIPES_PIPE.get());
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Ice", LangConf.Key.PIPES_ICE.get() + " " + LangConf.Key.PIPES_PIPE.get());
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Void", LangConf.Key.PIPES_VOID.get() + " " + LangConf.Key.PIPES_PIPE.get());
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Extraction", LangConf.Key.PIPES_EXTRACTION.get() + " " + LangConf.Key.PIPES_PIPE.get());
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Crafting", LangConf.Key.PIPES_CRAFTING.get() + " " + LangConf.Key.PIPES_PIPE.get());
        pipeBaseDuctType.registerDuctType(pipeType);

        //connect correctly
        pipeBaseDuctType.ductTypeOf("White").connectToAll();
        pipeBaseDuctType.ductTypeOf("Blue").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Blue");
        pipeBaseDuctType.ductTypeOf("Red").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Red");
        pipeBaseDuctType.ductTypeOf("Yellow").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Yellow");
        pipeBaseDuctType.ductTypeOf("Green").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Green");
        pipeBaseDuctType.ductTypeOf("Black").connectToAll().disconnectFromClasses(ColoredPipeType.class).connectTo("White", "Black");
        pipeBaseDuctType.ductTypeOf("Golden").connectToAll();
        pipeBaseDuctType.ductTypeOf("Iron").connectToAll();
        pipeBaseDuctType.ductTypeOf("Ice").connectToAll();
        pipeBaseDuctType.ductTypeOf("Void").connectToAll();
        pipeBaseDuctType.ductTypeOf("Extraction").connectToAll();
        pipeBaseDuctType.ductTypeOf("Crafting").connectToAll();

    }

    @Override
    public void registerRecipes() {
        BaseDuctType<Pipe> pipeBaseDuctType = ductRegister.baseDuctTypeOf("pipe");

        DuctType ductType;

        ductType = pipeBaseDuctType.ductTypeOf("White");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "a a", " a "}, 'a', new MaterialData(Material.GLASS)));
        ductType = pipeBaseDuctType.ductTypeOf("Blue");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.INK_SACK, DyeColor.BLUE.getDyeData())));
        ductType = pipeBaseDuctType.ductTypeOf("Red");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.INK_SACK, DyeColor.RED.getDyeData())));
        ductType = pipeBaseDuctType.ductTypeOf("Yellow");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.INK_SACK, DyeColor.YELLOW.getDyeData())));
        ductType = pipeBaseDuctType.ductTypeOf("Green");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.INK_SACK, DyeColor.GREEN.getDyeData())));
        ductType = pipeBaseDuctType.ductTypeOf("Black");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.INK_SACK, DyeColor.BLACK.getDyeData())));
        ductType = pipeBaseDuctType.ductTypeOf("Golden");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.GOLD_BLOCK)));
        ductType = pipeBaseDuctType.ductTypeOf("Iron");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.IRON_BLOCK)));
        ductType = pipeBaseDuctType.ductTypeOf("Ice");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.SNOW_BLOCK)));
        ductType = pipeBaseDuctType.ductTypeOf("Void");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.OBSIDIAN)));
        ductType = pipeBaseDuctType.ductTypeOf("Extraction");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.WOOD)));
        ductType = pipeBaseDuctType.ductTypeOf("Crafting");
        ductType.setDuctRecipe(itemService.createShapedRecipe(pipeBaseDuctType.getItemManager().getClonedItem(ductType), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.GLASS), 'b', new MaterialData(Material.WORKBENCH)));

        ShapedRecipe wrenchRecipe = itemService.createShapedRecipe(itemService.getWrench(), new String[]{" a ", "aba", " a "}, 'a', new MaterialData(Material.REDSTONE), 'b', new MaterialData(Material.STICK));
        Bukkit.addRecipe(wrenchRecipe);
    }

    @Override
    public void tick() {
        tickCounter++;
        tickCounter %= BIG_TICK_COUNT;
        boolean bigTick = tickCounter == 0;

        if(bigTick) {
            transportPipes.runTaskSync(() -> {
                Set<World> worlds = globalDuctManager.getDucts().keySet();
                synchronized (globalDuctManager.getDucts()) {
                    for (World world : worlds) {
                        Map<BlockLocation, Duct> ductMap = globalDuctManager.getDucts().get(world);
                        if (ductMap != null) {
                            for (Duct duct : ductMap.values()) {
                                if (duct instanceof Pipe && duct.isInLoadedChunk()) {
                                    duct.syncBigTick(this);
                                }
                            }
                        }
                    }
                }
            });
        }

        Set<World> worlds = globalDuctManager.getDucts().keySet();
        synchronized (globalDuctManager.getDucts()) {
            for (World world : worlds) {
                Map<BlockLocation, Duct> ductMap = globalDuctManager.getDucts().get(world);
                if (ductMap != null) {
                    for (Duct duct : ductMap.values()) {
                        if (duct instanceof Pipe && duct.isInLoadedChunk()) {
                            duct.tick(bigTick, transportPipes, this);
                        }
                    }
                    for (Duct duct : ductMap.values()) {
                        if (duct instanceof Pipe && duct.isInLoadedChunk()) {
                            duct.postTick(bigTick, transportPipes, this);
                        }
                    }
                }
            }
        }

    }

    public Set<PipeItem> getPlayerPipeItems(Player player) {
        return playerItems.computeIfAbsent(player, p -> Collections.synchronizedSet(new HashSet<>()));
    }

    public void spawnPipeItem(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            if (p.getLocation().distance(pipeItem.getBlockLoc().toLocation(pipeItem.getWorld())) <= Constants.DEFAULT_RENDER_DISTANCE) {
                getPlayerPipeItems(p).add(pipeItem);
                protocolService.sendPipeItem(p, pipeItem);
            }
        }
    }

    public void putPipeItemInPipe(PipeItem pipeItem) {
        Pipe pipeAtBlockLoc = (Pipe) globalDuctManager.getDuctAtLoc(pipeItem.getWorld(), pipeItem.getBlockLoc());
        if (pipeAtBlockLoc == null) {
            throw new IllegalStateException("pipe item can't be created because on the given location is no pipe");
        }
        pipeAtBlockLoc.putPipeItem(pipeItem);
    }

    public void updatePipeItemPosition(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            if (getPlayerPipeItems(p).contains(pipeItem)) {
                protocolService.updatePipeItem(p, pipeItem);
            }
        }
    }

    public void despawnPipeItem(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            if (getPlayerPipeItems(p).remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
    }

    @Override
    public void notifyDuctShown(Duct duct, Player p) {
        super.notifyDuctShown(duct, p);
        Pipe pipe = (Pipe) duct;
        Set<PipeItem> playerPipeItems = getPlayerPipeItems(p);
        for (PipeItem pipeItem : pipe.getItems()) {
            if (playerPipeItems.add(pipeItem)) {
                protocolService.sendPipeItem(p, pipeItem);
            }
        }
    }

    @Override
    public void notifyDuctHidden(Duct duct, Player p) {
        super.notifyDuctHidden(duct, p);
        Pipe pipe = (Pipe) duct;
        Set<PipeItem> playerPipeItems = getPlayerPipeItems(p);
        for (PipeItem pipeItem : pipe.getItems()) {
            if (playerPipeItems.remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
        for (PipeItem pipeItem : pipe.getFutureItems()) {
            if (playerPipeItems.remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
        for (PipeItem pipeItem : pipe.getUnloadedItems()) {
            if (playerPipeItems.remove(pipeItem)) {
                protocolService.removePipeItem(p, pipeItem);
            }
        }
    }
}
