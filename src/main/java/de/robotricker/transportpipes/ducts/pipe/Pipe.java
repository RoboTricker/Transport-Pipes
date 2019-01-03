package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

    public Pipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv) {
        super(ductType, blockLoc, world, chunk, settingsInv);
        items = Collections.synchronizedList(new ArrayList<>());
        futureItems = Collections.synchronizedList(new ArrayList<>());
    }

    public List<PipeItem> getItems() {
        return items;
    }

    public List<PipeItem> getFutureItems() {
        return futureItems;
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
                    if (possibleMovingDirs.isEmpty()) {
                        possibleMovingDirs.add(pipeItem.getMovingDir());
                    }

                    TPDirection newMovingDir = possibleMovingDirs.get((int) (Math.random() * possibleMovingDirs.size()));
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
                        pipe.putPipeItem(pipeItem);
                    } else {
                        items.remove(pipeItem);
                        pipeManager.destroyPipeItem(pipeItem);
                        if (tpContainer != null) {
                            //TODO: put into container
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

    @Override
    public void destroyed(TransportPipes transportPipes, DuctManager ductManager) {
        super.destroyed(transportPipes, ductManager);
        List<ItemStack> dropItems = new ArrayList<>();
        synchronized (futureItems) {
            Iterator<PipeItem> pipeItemIterator = futureItems.iterator();
            while (pipeItemIterator.hasNext()) {
                PipeItem pipeItem = pipeItemIterator.next();

                dropItems.add(pipeItem.getItem());
                ((PipeManager) ductManager).destroyPipeItem(pipeItem);

                pipeItemIterator.remove();
            }
        }
        synchronized (items) {
            Iterator<PipeItem> pipeItemIterator = items.iterator();
            while (pipeItemIterator.hasNext()) {
                PipeItem pipeItem = pipeItemIterator.next();

                dropItems.add(pipeItem.getItem());
                ((PipeManager) ductManager).destroyPipeItem(pipeItem);

                pipeItemIterator.remove();
            }
        }
        transportPipes.runTaskSync(() -> {
            for (ItemStack is : dropItems) {
                getWorld().dropItem(getBlockLoc().toLocation(getWorld()), is);
            }
        });
    }

    @Override
    public PipeType getDuctType() {
        return (PipeType) super.getDuctType();
    }

}
