package de.robotricker.transportpipes.rendersystem.pipe.modelled;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.ResourcePackRenderSystem;
import de.robotricker.transportpipes.rendersystem.pipe.modelled.model.ModelledPipeModel;
import de.robotricker.transportpipes.rendersystem.pipe.modelled.model.data.ModelledPipeConnectionModelData;
import de.robotricker.transportpipes.location.Direction;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;

public class ModelledPipeRenderSystem extends ResourcePackRenderSystem {

    private ModelledPipeModel model = new ModelledPipeModel();

    private Map<Pipe, ArmorStandData> midASD = new HashMap<>();
    private Map<Pipe, Map<Direction, ArmorStandData>> connASD = new HashMap<>();
    private AxisAlignedBB midAABB;
    private Map<Direction, AxisAlignedBB> connAABBs = new HashMap<>();

    public ModelledPipeRenderSystem() {
        super(BaseDuctType.valueOf("Pipe"));
        ModelledPipeModel.init();

        midAABB = new AxisAlignedBB(4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d);
        connAABBs.put(Direction.NORTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 0d / 16d, 12d / 16d, 12d / 16d, 4d / 16d));
        connAABBs.put(Direction.EAST, new AxisAlignedBB(12d / 16d, 4d / 16d, 4d / 16d, 16d / 16d, 12d / 16d, 12d / 16d));
        connAABBs.put(Direction.SOUTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d, 16d / 16d));
        connAABBs.put(Direction.WEST, new AxisAlignedBB(0d / 16d, 4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d));
        connAABBs.put(Direction.UP, new AxisAlignedBB(4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d, 16d / 16d, 12d / 16d));
        connAABBs.put(Direction.DOWN, new AxisAlignedBB(4d / 16d, 0d / 16d, 4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d));

    }

    @Override
    public void createDuctASD(Duct duct, Collection<Direction> connections) {
        Pipe pipe = (Pipe) duct;
        if (midASD.containsKey(pipe)) {
            return;
        }

        midASD.put(pipe, model.createMidASD(pipe.getDuctType()));
        Map<Direction, ArmorStandData> connMap = new HashMap<>();
        for (Direction connDir : connections) {
            connMap.put(connDir, model.createConnASD(ModelledPipeConnectionModelData.createConnectionModelData(pipe, connDir)));
        }
        connASD.put(pipe, connMap);
    }

    @Override
    public void updateDuctASD(Duct duct, Collection<Direction> connections, List<ArmorStandData> removeASD, List<ArmorStandData> addASD) {
        Pipe pipe = (Pipe) duct;
        if (!midASD.containsKey(pipe) || !connASD.containsKey(pipe)) {
            return;
        }
        Map<Direction, ArmorStandData> connMap = connASD.get(pipe);

        for (Direction connDir : Direction.values()) {
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
        Collection<Direction> pipeConns = pipe.getAllConnections();
        for (Direction connDir : pipeConns) {
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
    public Direction getClickedDuctFace(Player player, Duct duct) {
        Pipe pipe = (Pipe) duct;
        Vector ray = player.getEyeLocation().getDirection();
        Vector origin = player.getEyeLocation().toVector();

        Collection<Direction> connDirs = pipe.getAllConnections();
        Direction clickedMidFace = midAABB.performRayIntersection(ray, origin, pipe.getBlockLoc());
        if (clickedMidFace != null && !connDirs.contains(clickedMidFace)) {
            return clickedMidFace;
        } else {
            double nearestDistanceSquared = Double.MAX_VALUE;
            Direction currentClickedConnFace = null;
            for (Direction connDir : connDirs) {
                AxisAlignedBB connAABB = connAABBs.get(connDir);
                double newDistanceSquared = connAABB.getAABBMiddle(pipe.getBlockLoc()).distanceSquared(origin);
                if (newDistanceSquared < nearestDistanceSquared) {
                    Direction clickedConnFace = connAABB.performRayIntersection(ray, origin, pipe.getBlockLoc());
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
