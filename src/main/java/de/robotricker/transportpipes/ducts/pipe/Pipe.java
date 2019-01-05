package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.manager.DuctManager;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.manager.PipeManager;
import de.robotricker.transportpipes.ducts.pipe.items.PipeItem;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.ducts.types.pipetype.PipeType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class Pipe extends Duct {

    private static final int MAX_ITEMS = 20;

    /**
     * THREAD-SAFE
     * contains all the items that are inside this pipe and should be updated
     */
    private final List<PipeItem> items;
    /**
     * THREAD-SAFE
     * contains all the items that are just put inside this pipe and should be updated and put into the items list the next tick
     */
    private final List<PipeItem> futureItems;

    /**
     * THREAD-SAFE
     * contains all the items that could not be put into the next pipe or container because it is inside an unloaded chunk.
     * As this pipe / container gets loaded again, these items get put into the it one by one.
     */
    private final List<PipeItem> unloadedItems;

    private Map<TPDirection, TPContainer> connectedContainers;

    private int middleDirectionCounter = 0;

    public Pipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager);
        items = Collections.synchronizedList(new ArrayList<>());
        futureItems = Collections.synchronizedList(new ArrayList<>());
        unloadedItems = Collections.synchronizedList(new ArrayList<>());

        this.connectedContainers = Collections.synchronizedMap(new HashMap<>());
    }

    public Map<TPDirection, TPContainer> getContainerConnections() {
        return connectedContainers;
    }

    @Override
    public Set<TPDirection> getAllConnections() {
        Set<TPDirection> allConnections = super.getAllConnections();
        allConnections.addAll(getContainerConnections().keySet());
        return allConnections;
    }

    public List<PipeItem> getItems() {
        return items;
    }

    public List<PipeItem> getFutureItems() {
        return futureItems;
    }

    public List<PipeItem> getUnloadedItems() {
        return unloadedItems;
    }

    public void putPipeItem(PipeItem pipeItem) {
        futureItems.add(pipeItem);
    }

    double getPipeItemSpeed() {
        return 0.125d;
    }

    @Override
    public void tick(TransportPipes transportPipes, DuctManager ductManager, GlobalDuctManager globalDuctManager) {
        super.tick(transportPipes, ductManager, globalDuctManager);

        PipeManager pipeManager = (PipeManager) ductManager;
        if (items.size() > MAX_ITEMS) {
            transportPipes.runTaskAsync(() -> globalDuctManager.destroyDuct(this, null), 0L);
            return;
        }
        synchronized (items) {
            for (int i = items.size() - 1; i >= 0; i--) {
                PipeItem pipeItem = items.get(i);

                long factor = (long) (getPipeItemSpeed() * RelativeLocation.PRECISION);
                pipeItem.getRelativeLocation().add(pipeItem.getMovingDir().getX() * factor, pipeItem.getMovingDir().getY() * factor, pipeItem.getMovingDir().getZ() * factor);
                pipeManager.updatePipeItem(pipeItem);
                pipeItem.resetOldRelativeLocation();

                if (pipeItem.getRelativeLocation().isEquals(0.5d, 0.5d, 0.5d)) {
                    //arrival at middle

                    //calculate possible moving directions
                    List<TPDirection> possibleMovingDirs = new ArrayList<>(getAllConnections());
                    possibleMovingDirs.remove(pipeItem.getMovingDir().getOpposite());

                    //calculate actual outcome
                    TPDirection newMovingDir = calculateMiddleOutcome(pipeItem, possibleMovingDirs);
                    pipeItem.setMovingDir(newMovingDir);
                } else if (pipeItem.getRelativeLocation().isXEquals(0d) || pipeItem.getRelativeLocation().isYEquals(0d) || pipeItem.getRelativeLocation().isZEquals(0d)
                        || pipeItem.getRelativeLocation().isXEquals(1d) || pipeItem.getRelativeLocation().isYEquals(1d) || pipeItem.getRelativeLocation().isZEquals(1d)) {
                    //arrival at end of pipe
                    Duct duct = getDuctConnections().get(pipeItem.getMovingDir());
                    TPContainer tpContainer = getContainerConnections().get(pipeItem.getMovingDir());
                    if (duct instanceof Pipe) {
                        Pipe pipe = (Pipe) duct;

                        //make pipe item ready for next pipe
                        pipeItem.setBlockLoc(pipe.getBlockLoc());
                        pipeItem.getRelativeLocation().switchValues();
                        pipeItem.resetOldRelativeLocation();

                        //remove from current pipe and add to new one
                        items.remove(pipeItem);
                        if (pipe.isInLoadedChunk()) {
                            pipe.putPipeItem(pipeItem);
                        } else {
                            unloadedItems.add(pipeItem);
                        }
                    } else {
                        items.remove(pipeItem);
                        pipeManager.destroyPipeItem(pipeItem);
                        if (tpContainer != null) {

                            pipeItem.setBlockLoc(getBlockLoc().getNeighbor(pipeItem.getMovingDir()));
                            pipeItem.getRelativeLocation().switchValues();
                            pipeItem.resetOldRelativeLocation();

                            transportPipes.runTaskSync(() -> {
                                if (tpContainer.isInLoadedChunk()) {
                                    ItemStack overflow = tpContainer.insertItem(pipeItem.getMovingDir(), pipeItem.getItem());
                                    if (overflow != null) {
                                        getWorld().dropItem(getBlockLoc().toLocation(getWorld()), overflow);
                                    }
                                } else {
                                    unloadedItems.add(pipeItem);
                                }
                            });
                        } else {
                            //drop item
                            transportPipes.runTaskSync(() -> {
                                pipeItem.getWorld().dropItem(pipeItem.getBlockLoc().getNeighbor(pipeItem.getMovingDir()).toLocation(pipeItem.getWorld()), pipeItem.getItem());
                            });
                        }
                    }
                }

            }
        }
    }

    public TPDirection calculateMiddleOutcome(PipeItem pipeItem, List<TPDirection> possibleMovingDirs) {
        if (possibleMovingDirs.isEmpty()) {
            return pipeItem.getMovingDir();
        }

        List<TPDirection> possibleMovingDirWithSize = new ArrayList<>();

        for (TPDirection possibleMovingDir : possibleMovingDirs) {
            boolean space = false;
            if (getContainerConnections().containsKey(possibleMovingDir)) {
                TPContainer container = getContainerConnections().get(possibleMovingDir);
                space = container.spaceForItem(possibleMovingDir, pipeItem.getItem()) >= pipeItem.getItem().getAmount();
            } else if (getDuctConnections().containsKey(possibleMovingDir)) {
                Duct duct = getDuctConnections().get(possibleMovingDir);
                if (duct instanceof Pipe) {
                    space = true;
                }
            }
            if (space) {
                possibleMovingDirWithSize.add(possibleMovingDir);
            }
        }

        if (possibleMovingDirWithSize.isEmpty()) {
            possibleMovingDirWithSize.addAll(possibleMovingDirs);
        }

        middleDirectionCounter %= possibleMovingDirWithSize.size();
        return possibleMovingDirWithSize.get(middleDirectionCounter++);
    }

    @Override
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> dropItems = super.destroyed(transportPipes, ductManager, destroyer);

        synchronized (items) {
            items.forEach(pipeItem -> {
                ((PipeManager) ductManager).destroyPipeItem(pipeItem);
                dropItems.add(pipeItem.getItem());
            });
            items.clear();
        }
        synchronized (futureItems) {
            futureItems.forEach(pipeItem -> {
                ((PipeManager) ductManager).destroyPipeItem(pipeItem);
                dropItems.add(pipeItem.getItem());
            });
            futureItems.clear();
        }
        synchronized (unloadedItems) {
            unloadedItems.forEach(pipeItem -> {
                ((PipeManager) ductManager).destroyPipeItem(pipeItem);
                dropItems.add(pipeItem.getItem());
            });
            unloadedItems.clear();
        }

        return dropItems;
    }

    @Override
    public PipeType getDuctType() {
        return (PipeType) super.getDuctType();
    }

}
