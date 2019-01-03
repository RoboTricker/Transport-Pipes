package de.robotricker.transportpipes.ducts.manager;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.pipe.items.PipeItem;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.ducts.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.ducts.types.pipetype.PipeType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.utils.WorldUtils;

public class PipeManager extends DuctManager<Pipe> {

    /**
     * THREAD-SAFE
     */
    private Map<Player, Set<PipeItem>> playerItems;

    @Inject
    public PipeManager(TransportPipes transportPipes, DuctRegister ductRegister, GlobalDuctManager globalDuctManager, ProtocolService protocolService) {
        super(transportPipes, ductRegister, globalDuctManager, protocolService);
        playerItems = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void registerDuctTypes() {
        PipeType pipeType;
        BaseDuctType<Pipe> pipeBaseDuctType = ductRegister.baseDuctTypeOf("pipe");

        pipeType = new ColoredPipeType(pipeBaseDuctType, "White", '7');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Blue", '1');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Red", '4');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Yellow", 'e');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Green", '2');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new ColoredPipeType(pipeBaseDuctType, "Black", '8');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Golden", '6');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Iron", '7');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Ice", 'b');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Void", '5');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Extraction", 'd');
        pipeBaseDuctType.registerDuctType(pipeType);
        pipeType = new PipeType(pipeBaseDuctType, "Crafting", 'e');
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
    public void tick(Map<World, Map<BlockLocation, Duct>> ducts) {
        for (World world : ducts.keySet()) {
            Map<BlockLocation, Duct> ductMap = ducts.get(world);
            if (ductMap != null) {
                synchronized (ductMap) {
                    // activate pipeItems which are in futureItems
                    for (Duct duct : ductMap.values()) {
                        Pipe pipe = (Pipe) duct;
                        synchronized (pipe.getFutureItems()) {
                            Iterator<PipeItem> itemIt = pipe.getFutureItems().iterator();
                            while (itemIt.hasNext()) {
                                PipeItem futureItem = itemIt.next();
                                pipe.getItems().add(futureItem);
                                itemIt.remove();
                            }
                        }
                    }
                    //normal tick and item update
                    for (Duct duct : ductMap.values()) {
                        if (duct.isInLoadedChunk()) {
                            duct.tick(transportPipes, this, globalDuctManager);
                        }
                    }
                }
            }
        }
    }

    public Set<PipeItem> getPlayerPipeItems(Player player) {
        return playerItems.computeIfAbsent(player, p -> Collections.synchronizedSet(new HashSet<>()));
    }

    public void createPipeItem(PipeItem pipeItem) {
        Pipe pipeAtBlockLoc = (Pipe) globalDuctManager.getDuctAtLoc(pipeItem.getWorld(), pipeItem.getBlockLoc());
        if (pipeAtBlockLoc == null) {
            throw new IllegalStateException("pipe item can't be created because on the given location is no pipe");
        }

        pipeAtBlockLoc.putPipeItem(pipeItem);
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            getPlayerPipeItems(p).add(pipeItem);
            protocolService.sendPipeItem(p, pipeItem);
        }
    }

    public void updatePipeItem(PipeItem pipeItem) {
        List<Player> playerList = WorldUtils.getPlayerList(pipeItem.getWorld());
        for (Player p : playerList) {
            if (getPlayerPipeItems(p).contains(pipeItem)) {
                protocolService.updatePipeItem(p, pipeItem);
            }
        }
    }

    public void destroyPipeItem(PipeItem pipeItem) {
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
    }
}
