package de.robotricker.transportpipes.rendersystem.pipe.vanilla;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.VanillaPipeModel;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.VanillaPipeModelEW;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.VanillaPipeModelMID;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.VanillaPipeModelNS;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.VanillaPipeModelUD;
import de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.data.VanillaPipeModelData;
import de.robotricker.transportpipes.location.Direction;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;

public class VanillaPipeRenderSystem extends RenderSystem {

    private VanillaPipeModel midModel = new VanillaPipeModelMID();
    private VanillaPipeModel ewModel = new VanillaPipeModelEW();
    private VanillaPipeModel nsModel = new VanillaPipeModelNS();
    private VanillaPipeModel udModel = new VanillaPipeModelUD();

    private Map<Pipe, List<ArmorStandData>> pipeASD = new HashMap<>();

    public VanillaPipeRenderSystem() {
        super(BaseDuctType.valueOf("Pipe"));
        VanillaPipeModel.init();
    }

    @Override
    public void createDuctASD(Duct duct, Collection<Direction> connections) {
        Pipe pipe = (Pipe) duct;
        if (pipeASD.containsKey(pipe)) {
            return;
        }
        pipeASD.put(pipe, getModel(pipe.getDuctType(), connections).createASD(VanillaPipeModelData.createModelData(pipe)));
    }

    @Override
    public void updateDuctASD(Duct duct, Collection<Direction> connections, List<ArmorStandData> removeASD, List<ArmorStandData> addASD) {
        Pipe pipe = (Pipe) duct;
        if (!pipeASD.containsKey(pipe)) {
            return;
        }

        List<ArmorStandData> oldASD = pipeASD.get(pipe);
        List<ArmorStandData> newASD = getModel(pipe.getDuctType(), connections).createASD(VanillaPipeModelData.createModelData(pipe));

        List<ArmorStandData> tempASD = new ArrayList<>();

        for (int i = 0; i < Math.max(oldASD.size(), newASD.size()); i++) {
            ArmorStandData ASDOld = i < oldASD.size() ? oldASD.get(i) : null;
            ArmorStandData ASDNew = i < newASD.size() ? newASD.get(i) : null;
            if (!((ASDOld != null && ASDOld.isSimilar(ASDNew)) || ASDOld == ASDNew)) {
                if (ASDOld != null) {
                    removeASD.add(ASDOld);
                }
                if (ASDNew != null) {
                    addASD.add(ASDNew);
                    tempASD.add(ASDNew);
                }
            } else if (ASDOld != null) {
                tempASD.add(ASDOld);
            }
        }

        oldASD.clear();
        oldASD.addAll(tempASD);
    }

    @Override
    public void destroyDuctASD(Duct duct) {
        Pipe pipe = (Pipe) duct;
        pipeASD.remove(pipe);
    }

    @Override
    public List<ArmorStandData> getASDForDuct(Duct duct) {
        Pipe pipe = (Pipe) duct;
        return pipeASD.get(pipe);
    }

    @Override
    public AxisAlignedBB getOuterHitbox(Duct duct) {
        Pipe pipe = (Pipe) duct;
        return getModel(pipe.getDuctType(), null).getAABB(); //TODO: calc all connections
    }

    @Override
    public Direction getClickedDuctFace(Player player, Duct duct) {
        return getOuterHitbox(duct).performRayIntersection(player.getEyeLocation().getDirection(), player.getEyeLocation().toVector(), duct.getBlockLoc());
    }

    private VanillaPipeModel getModel(PipeType pt, Collection<Direction> conns) {
        List<Direction> connList = new ArrayList<>(conns);
        if (!(pt.is("Golden") || pt.is("Iron") || pt.is("Void") || pt.is("Crafting"))) {
            if (connList.size() == 1) {
                if (connList.get(0) == Direction.EAST || connList.get(0) == Direction.WEST) {
                    return ewModel;
                } else if (connList.get(0) == Direction.NORTH || connList.get(0) == Direction.SOUTH) {
                    return nsModel;
                } else if (connList.get(0) == Direction.UP || connList.get(0) == Direction.DOWN) {
                    return udModel;
                }
            } else if (connList.size() == 2 && connList.get(0).getOpposite().equals(connList.get(1))) {
                if (connList.get(0) == Direction.EAST || connList.get(0) == Direction.WEST) {
                    return ewModel;
                } else if (connList.get(0) == Direction.NORTH || connList.get(0) == Direction.SOUTH) {
                    return nsModel;
                } else if (connList.get(0) == Direction.UP || connList.get(0) == Direction.DOWN) {
                    return udModel;
                }
            }
        }
        return midModel;
    }

}