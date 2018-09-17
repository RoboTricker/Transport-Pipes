package de.robotricker.transportpipes.rendersystems;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.utils.TPDirection;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;

public abstract class RenderSystem {

    private DuctType ductType;

    public RenderSystem(DuctType ductType) {
        this.ductType = ductType;
    }

    // ***************************************************************
    // ASD UTILS
    // ***************************************************************

    /**
     * creates the ASD information of this duct in respect of its connections and saves it internally.
     * This is only for duct creation, not for duct updates!
     */
    public abstract void createDuctASD(Duct duct, Collection<TPDirection> connections);

    /**
     * does the same as createDuctASD but for ducts that exist already. The difference in ASD is put into "removeASD" and "addASD" so the caller can send it to the clients.
     */
    public abstract void updateDuctASD(Duct duct, Collection<TPDirection> connections, List<ArmorStandData> removeASD, List<ArmorStandData> addASD);

    /**
     * simply removes the ASD information of this duct
     */
    public abstract void destroyDuctASD(Duct duct);

    /**
     * retrieves all ASD information of the given duct and returns it
     */
    public abstract List<ArmorStandData> getASDForDuct(Duct duct);

    // ***************************************************************
    // AABB UTILS
    // ***************************************************************

    public abstract AxisAlignedBB getOuterHitbox(Duct duct);

    public abstract TPDirection getClickedDuctFace(Player player, Duct duct);

    // ***************************************************************
    //  MISC
    // ***************************************************************

    public DuctType getDuctType(){
        return ductType;
    }

}
