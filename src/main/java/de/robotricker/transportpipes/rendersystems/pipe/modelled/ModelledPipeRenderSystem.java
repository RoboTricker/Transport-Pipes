package de.robotricker.transportpipes.rendersystems.pipe.modelled;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.ModelledRenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.ModelledPipeModel;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledPipeConnectionModelData;

public class ModelledPipeRenderSystem extends ModelledRenderSystem {

    private ModelledPipeModel model = new ModelledPipeModel();

    private Map<Pipe, ArmorStandData> midASD = new HashMap<>();
    private Map<Pipe, Map<TPDirection, ArmorStandData>> connASD = new HashMap<>();
    private AxisAlignedBB midAABB;
    private Map<TPDirection, AxisAlignedBB> connAABBs = new HashMap<>();

    @Inject
    public ModelledPipeRenderSystem(ItemService itemService, DuctRegister ductRegister) {
        super(ductRegister.baseDuctTypeOf("Pipe"));
        ModelledPipeModel.init(itemService, ductRegister);

        midAABB = new AxisAlignedBB(4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d);
        connAABBs.put(TPDirection.NORTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 0d / 16d, 12d / 16d, 12d / 16d, 4d / 16d));
        connAABBs.put(TPDirection.EAST, new AxisAlignedBB(12d / 16d, 4d / 16d, 4d / 16d, 16d / 16d, 12d / 16d, 12d / 16d));
        connAABBs.put(TPDirection.SOUTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d, 16d / 16d));
        connAABBs.put(TPDirection.WEST, new AxisAlignedBB(0d / 16d, 4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d));
        connAABBs.put(TPDirection.UP, new AxisAlignedBB(4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d, 16d / 16d, 12d / 16d));
        connAABBs.put(TPDirection.DOWN, new AxisAlignedBB(4d / 16d, 0d / 16d, 4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d));

    }

    @Override
    public void createDuctASD(Duct duct, Collection<TPDirection> connections) {
        Pipe pipe = (Pipe) duct;
        if (midASD.containsKey(pipe)) {
            return;
        }

        midASD.put(pipe, model.createMidASD(pipe.getDuctType()));
        Map<TPDirection, ArmorStandData> connMap = new HashMap<>();
        for (TPDirection connDir : connections) {
            connMap.put(connDir, model.createConnASD(ModelledPipeConnectionModelData.createConnectionModelData(pipe, connDir)));
        }
        connASD.put(pipe, connMap);
    }

    @Override
    public void updateDuctASD(Duct duct, Collection<TPDirection> connections, List<ArmorStandData> removeASD, List<ArmorStandData> addASD) {
        Pipe pipe = (Pipe) duct;
        if (!midASD.containsKey(pipe) || !connASD.containsKey(pipe)) {
            return;
        }
        Map<TPDirection, ArmorStandData> connMap = connASD.get(pipe);

        for (TPDirection connDir : TPDirection.values()) {
            if (connMap.containsKey(connDir) && connections.contains(connDir)) {
                // direction was active before and after update
                ArmorStandData newASD = model.createConnASD(ModelledPipeConnectionModelData.createConnectionModelData(pipe, connDir));
                if (!connMap.get(connDir).isSimilar(newASD)) {
                    // ASD changed after update in this direction
                    removeASD.add(connMap.get(connDir));
                    addASD.add(newASD);
                    connMap.put(connDir, newASD);
                }
            } else if (!connMap.containsKey(connDir) && connections.contains(connDir)) {
                // direction wasn't active before update but direction IS active after update
                ArmorStandData newASD = model.createConnASD(ModelledPipeConnectionModelData.createConnectionModelData(pipe, connDir));
                addASD.add(newASD);
                connMap.put(connDir, newASD);
            } else if (connMap.containsKey(connDir) && !connections.contains(connDir)) {
                // direction was active before update but isn't active after update
                removeASD.add(connMap.get(connDir));
                connMap.remove(connDir);
            }
        }
    }

    @Override
    public void destroyDuctASD(Duct duct) {
        Pipe pipe = (Pipe) duct;
        midASD.remove(pipe);
        connASD.remove(pipe);
    }

    @Override
    public List<ArmorStandData> getASDForDuct(Duct duct) {
        Pipe pipe = (Pipe) duct;

        List<ArmorStandData> asd = new ArrayList<>();
        if (midASD.containsKey(pipe)) {
            asd.add(midASD.get(pipe));
        }
        if (connASD.containsKey(pipe)) {
            asd.addAll(connASD.get(pipe).values());
        }
        return asd;
    }

    @Override
    public AxisAlignedBB getOuterHitbox(Duct duct) {
        Pipe pipe = (Pipe) duct;
        if (pipe == null) {
            return null;
        }
        List<AxisAlignedBB> aabbs = new ArrayList<>();
        aabbs.add(midAABB);
        Collection<TPDirection> pipeConns = pipe.getAllConnections();
        for (TPDirection connDir : pipeConns) {
            aabbs.add(connAABBs.get(connDir));
        }
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double minz = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;
        double maxz = Double.MIN_VALUE;
        for (AxisAlignedBB aabb : aabbs) {
            minx = Math.min(aabb.getMin().getDoubleX(), minx);
            miny = Math.min(aabb.getMin().getDoubleY(), miny);
            minz = Math.min(aabb.getMin().getDoubleZ(), minz);
            maxx = Math.max(aabb.getMax().getDoubleX(), maxx);
            maxy = Math.max(aabb.getMax().getDoubleY(), maxy);
            maxz = Math.max(aabb.getMax().getDoubleZ(), maxz);
        }
        return new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz);
    }

    @Override
    public TPDirection getClickedDuctFace(Player player, Duct duct) {
        Pipe pipe = (Pipe) duct;
        Vector ray = player.getEyeLocation().getDirection();
        Vector origin = player.getEyeLocation().toVector();

        Collection<TPDirection> connDirs = pipe.getAllConnections();
        TPDirection clickedMidFace = midAABB.performRayIntersection(ray, origin, pipe.getBlockLoc());
        if (clickedMidFace != null && !connDirs.contains(clickedMidFace)) {
            return clickedMidFace;
        } else {
            double nearestDistanceSquared = Double.MAX_VALUE;
            TPDirection currentClickedConnFace = null;
            for (TPDirection connDir : connDirs) {
                AxisAlignedBB connAABB = connAABBs.get(connDir);
                double newDistanceSquared = connAABB.getAABBMiddle(pipe.getBlockLoc()).distanceSquared(origin);
                if (newDistanceSquared < nearestDistanceSquared) {
                    TPDirection clickedConnFace = connAABB.performRayIntersection(ray, origin, pipe.getBlockLoc());
                    if (clickedConnFace != null) {
                        nearestDistanceSquared = newDistanceSquared;
                        currentClickedConnFace = clickedConnFace;
                    }
                }
            }
            return currentClickedConnFace;
        }
    }

}
