package de.robotricker.transportpipes.listener;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.InventoryHolder;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.BlockContainer;
import de.robotricker.transportpipes.container.BrewingStandContainer;
import de.robotricker.transportpipes.container.FurnaceContainer;
import de.robotricker.transportpipes.container.SimpleInventoryContainer;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.manager.PipeManager;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.utils.WorldUtils;

public class TPContainerListener implements Listener {

    @Inject
    private DuctRegister ductRegister;

    @Inject
    private GlobalDuctManager globalDuctManager;

    @Inject
    private TransportPipes transportPipes;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        notifyBlockUpdate(e.getBlock(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        notifyBlockUpdate(e.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        for (Block b : e.blockList()) {
            notifyBlockUpdate(b, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block b : e.blockList()) {
            notifyBlockUpdate(b, false);
        }
    }

    private void notifyBlockUpdate(Block block, boolean place) {
        if (WorldUtils.isContainerBlock(block.getType())) {
            updateContainerBlock(block, place, true);
        }
        if (!place) {
            //undo obfuscation
            Duct duct = globalDuctManager.getDuctAtLoc(block.getLocation());
            if (duct != null) {
                duct.obfuscatedWith(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        handleChunkLoadSync(e.getChunk(), false);
    }

    public void updateContainerBlock(Block block, boolean add, boolean updateNeighborPipes) {
        PipeManager pipeManager = (PipeManager) (DuctManager<? extends Duct>) ductRegister.baseDuctTypeOf("pipe").getDuctManager();

        BlockLocation blockLoc = new BlockLocation(block.getLocation());
        if (add) {

            if (pipeManager.getContainerAtLoc(block.getLocation()) == null) {
                TransportPipesContainer container = createContainerFromBlock(block);
                pipeManager.getContainers(block.getWorld()).put(blockLoc, container);

                // only update the neighbor pipes if this updateContainerBlock method call is because of a chunk load that was not issued inside the onEnable method
                if (updateNeighborPipes) {
                    for (TPDirection dir : TPDirection.values()) {
                        Duct duct = globalDuctManager.getDuctAtLoc(block.getWorld(), blockLoc.getNeighbor(dir));
                        if (duct instanceof Pipe) {
                            globalDuctManager.updateDuctConnections(duct);
                            globalDuctManager.updateDuctInRenderSystems(duct, true);
                        }
                    }
                }

                transportPipes.runTaskSync(() -> {
                    //checks for double chest neighbor and updates the neighbors TransportPipesContainer if present
                    Block neighborDoubleChestBlock = checkForDoubleChestNeighbor(block);
                    if (neighborDoubleChestBlock != null) {
                        TransportPipesContainer neighborContainer = pipeManager.getContainerAtLoc(neighborDoubleChestBlock.getWorld(), new BlockLocation(neighborDoubleChestBlock.getLocation()));
                        if (neighborContainer instanceof BlockContainer) {
                            ((BlockContainer) neighborContainer).updateBlock();
                        }
                        if (pipeManager.getContainers(block.getWorld()).get(blockLoc) == container) {
                            ((BlockContainer) container).updateBlock();
                        }
                    }
                });

            }

        } else {

            TransportPipesContainer container = pipeManager.getContainerAtLoc(block.getLocation());
            if (container != null) {
                pipeManager.getContainers(block.getWorld()).remove(blockLoc);

                // only update the neighbor pipes if this updateContainerBlock method call is because of a chunk load that was not issued inside the onEnable method
                if (updateNeighborPipes) {
                    for (TPDirection dir : TPDirection.values()) {
                        Duct duct = globalDuctManager.getDuctAtLoc(block.getWorld(), blockLoc.getNeighbor(dir));
                        if (duct instanceof Pipe) {
                            globalDuctManager.updateDuctConnections(duct);
                            globalDuctManager.updateDuctInRenderSystems(duct, true);
                        }
                    }
                }
            }
        }
    }

    public Block checkForDoubleChestNeighbor(Block block) {
        if (block.getState() instanceof InventoryHolder) {
            if (((InventoryHolder) block.getState()).getInventory().getHolder() instanceof DoubleChest) {
                for (TPDirection dir : TPDirection.values()) {
                    if (dir.isSide()) {
                        Block neighborBlock = block.getRelative(dir.getBlockFace());
                        if (neighborBlock.getState() instanceof InventoryHolder) {
                            if (((InventoryHolder) neighborBlock.getState()).getInventory().getHolder() instanceof DoubleChest) {
                                DoubleChest blockChest = (DoubleChest) ((InventoryHolder) block.getState()).getInventory().getHolder();
                                DoubleChest neighborBlockChest = (DoubleChest) ((InventoryHolder) neighborBlock.getState()).getInventory().getHolder();
                                if (blockChest.getLocation().equals(neighborBlockChest.getLocation())) {
                                    return neighborBlock;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public TransportPipesContainer createContainerFromBlock(Block block) {
        if (block.getState() instanceof Furnace) {
            return new FurnaceContainer(block);
        } else if (block.getState() instanceof BrewingStand) {
            return new BrewingStandContainer(block);
        } else if (block.getState() instanceof InventoryHolder) {
            return new SimpleInventoryContainer(block);
        }
        return null;
    }

    public void handleChunkLoadSync(Chunk loadedChunk, boolean onServerStart) {
        PipeManager pipeManager = (PipeManager) (DuctManager<? extends Duct>) ductRegister.baseDuctTypeOf("pipe").getDuctManager();

        if (loadedChunk.getTileEntities() != null) {
            for (BlockState bs : loadedChunk.getTileEntities()) {
                if (WorldUtils.isContainerBlock(bs.getType())) {

                    //automatically ignores this block if it is already registered as container block
                    updateContainerBlock(bs.getBlock(), true, !onServerStart);

                    //if this block is already registered, update the block, because the blockState object changes after a chunk unload and load
                    TransportPipesContainer container = pipeManager.getContainerAtLoc(bs.getLocation());
                    if (container instanceof BlockContainer) {
                        ((BlockContainer) container).updateBlock();
                    }
                }
            }
        }
    }


}
