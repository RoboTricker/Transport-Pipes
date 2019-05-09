package de.robotricker.transportpipes.api;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;

import javax.inject.Inject;

import de.robotricker.transportpipes.ThreadService;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.manager.PipeManager;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.listener.TPContainerListener;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.utils.WorldUtils;

public class TransportPipesAPI {

    private static TransportPipesAPI instance;

    @Inject
    private GlobalDuctManager globalDuctManager;

    @Inject
    private DuctRegister ductRegister;

    @Inject
    private ThreadService threadService;

    @Inject
    private TPContainerListener tpContainerListener;

    public TransportPipesAPI() {
        instance = this;
    }

    public void buildDuct(String baseDuctTypeName, String ductTypeName, BlockLocation blockLocation, World world, Chunk chunk) throws Exception {

        if (globalDuctManager.getDuctAtLoc(world, blockLocation) != null) {
            throw new Exception("Another duct exists at this location");
        }

        DuctType ductType = ductRegister.baseDuctTypeOf(baseDuctTypeName).ductTypeOf(ductTypeName);

        if (ductType.getBaseDuctType().is("pipe")) {
            for (TPDirection dir : TPDirection.values()) {
                if (WorldUtils.lwcProtection(blockLocation.toBlock(world).getRelative(dir.getBlockFace()))) {
                    throw new Exception("Cannot place duct next to protected container block");
                }
            }
        }

        Duct duct = globalDuctManager.createDuctObject(ductType, blockLocation, world, chunk);
        globalDuctManager.registerDuct(duct);
        globalDuctManager.updateDuctConnections(duct);
        globalDuctManager.registerDuctInRenderSystems(duct, true);
        globalDuctManager.updateNeighborDuctsConnections(duct);
        globalDuctManager.updateNeighborDuctsInRenderSystems(duct, true);
    }

    public void destroyDuct(BlockLocation blockLocation, World world) throws Exception {
        Duct duct = globalDuctManager.getDuctAtLoc(world, blockLocation);
        if (duct == null) {
            throw new Exception("There is no duct at this location");
        }
        globalDuctManager.unregisterDuct(duct);
        globalDuctManager.unregisterDuctInRenderSystem(duct, true);
        globalDuctManager.updateNeighborDuctsConnections(duct);
        globalDuctManager.updateNeighborDuctsInRenderSystems(duct, true);
        globalDuctManager.playDuctDestroyActions(duct, null);
    }

    public void putItemInPipe(Pipe pipe, ItemStack item, TPDirection direction) {
        ((PipeManager) (DuctManager<?>) ductRegister.baseDuctTypeOf("pipe").getDuctManager()).putPipeItemInPipe(new PipeItem(item, pipe.getWorld(), pipe.getBlockLoc(), direction));
    }

    public void registerTransportPipesContainer(TransportPipesContainer container, BlockLocation blockLocation, World world) throws Exception {
        PipeManager pipeManager = (PipeManager) (DuctManager<?>) ductRegister.baseDuctTypeOf("pipe").getDuctManager();
        if (pipeManager.getContainerAtLoc(blockLocation.toLocation(world)) != null || globalDuctManager.getDuctAtLoc(world, blockLocation) != null) {
            throw new Exception("The given location is not empty");
        }
        pipeManager.getContainers(world).put(blockLocation, container);

        for (TPDirection dir : TPDirection.values()) {
            Duct duct = globalDuctManager.getDuctAtLoc(world, blockLocation.getNeighbor(dir));
            if (duct instanceof Pipe) {
                globalDuctManager.updateDuctConnections(duct);
                globalDuctManager.updateDuctInRenderSystems(duct, true);
            }
        }
    }

    public void unregisterTransportPipesContainer(BlockLocation blockLocation, World world) throws Exception {
        PipeManager pipeManager = (PipeManager) (DuctManager<?>) ductRegister.baseDuctTypeOf("pipe").getDuctManager();
        TransportPipesContainer container = pipeManager.getContainerAtLoc(blockLocation.toLocation(world));
        if (container == null) {
            throw new Exception("There is no TransportPipesContainer at the given location");
        }
        pipeManager.getContainers(world).remove(blockLocation);

        for (TPDirection dir : TPDirection.values()) {
            Duct duct = globalDuctManager.getDuctAtLoc(world, blockLocation.getNeighbor(dir));
            if (duct instanceof Pipe) {
                globalDuctManager.updateDuctConnections(duct);
                globalDuctManager.updateDuctInRenderSystems(duct, true);
            }
        }
    }

    public int getTPS() {
        return threadService.getCurrentTPS();
    }

    public int getPreferredTPS() {
        return threadService.getPreferredTPS();
    }

    public void updateVanillaContainerBlock(Block block, boolean placed) {
        tpContainerListener.updateContainerBlock(block, placed, true);
    }

    public Duct getDuct(BlockLocation blockLocation, World world) {
        return globalDuctManager.getDuctAtLoc(world, blockLocation);
    }

    public int getDuctCount(World world) {
        return globalDuctManager.getDucts(world).size();
    }

    public static TransportPipesAPI getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("TransportPipes is not yet initialized");
        }
        return instance;
    }
}
