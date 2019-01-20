package de.robotricker.transportpipes.rendersystems.pipe.vanilla;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.pipetype.PipeType;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.VanillaRenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.VanillaPipeModel;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.VanillaPipeModelEW;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.VanillaPipeModelMID;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.VanillaPipeModelNS;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.VanillaPipeModelUD;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data.VanillaPipeModelData;

public class VanillaPipeRenderSystem extends VanillaRenderSystem {

    private VanillaPipeModel midModel = new VanillaPipeModelMID();
    private VanillaPipeModel ewModel = new VanillaPipeModelEW();
    private VanillaPipeModel nsModel = new VanillaPipeModelNS();
    private VanillaPipeModel udModel = new VanillaPipeModelUD();

    private Map<Pipe, List<ArmorStandData>> pipeASD = new HashMap<>();

    @Inject
    public VanillaPipeRenderSystem(DuctRegister ductRegister) {
        super(ductRegister.baseDuctTypeOf("Pipe"));
        VanillaPipeModel.init(ductRegister);
    }

    @Override
    public void createDuctASD(Duct duct, Collection<TPDirection> connections) {
        Pipe pipe = (Pipe) duct;
        if (pipeASD.containsKey(pipe)) {
            return;
        }
        pipeASD.put(pipe, getModel(pipe.getDuctType(), connections).createASD(VanillaPipeModelData.createModelData(pipe)));
    }

    @Override
    public void updateDuctASD(Duct duct, Collection<TPDirection> connections, List<ArmorStandData> removeASD, List<ArmorStandData> addASD) {
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
        return getModel(pipe.getDuctType(), duct.getAllConnections()).getAABB();
    }

    @Override
    public TPDirection getClickedDuctFace(Player player, Duct duct) {
        return getOuterHitbox(duct).performRayIntersection(player.getEyeLocation().getDirection(), player.getEyeLocation().toVector(), duct.getBlockLoc());
    }

    private VanillaPipeModel getModel(PipeType pt, Collection<TPDirection> conns) {
        List<TPDirection> connList = new ArrayList<>(conns);
        if (!(pt.is("Golden") || pt.is("Iron") || pt.is("Void") || pt.is("Crafting"))) {
            if (connList.size() == 1) {
                if (connList.get(0) == TPDirection.EAST || connList.get(0) == TPDirection.WEST) {
                    return ewModel;
                } else if (connList.get(0) == TPDirection.NORTH || connList.get(0) == TPDirection.SOUTH) {
                    return nsModel;
                } else if (connList.get(0) == TPDirection.UP || connList.get(0) == TPDirection.DOWN) {
                    return udModel;
                }
            } else if (connList.size() == 2 && connList.get(0).getOpposite().equals(connList.get(1))) {
                if (connList.get(0) == TPDirection.EAST || connList.get(0) == TPDirection.WEST) {
                    return ewModel;
                } else if (connList.get(0) == TPDirection.NORTH || connList.get(0) == TPDirection.SOUTH) {
                    return nsModel;
                } else if (connList.get(0) == TPDirection.UP || connList.get(0) == TPDirection.DOWN) {
                    return udModel;
                }
            }
        }
        return midModel;
    }

}