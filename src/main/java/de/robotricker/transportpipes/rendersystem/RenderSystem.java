package de.robotricker.transportpipes.rendersystem;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.location.Direction;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;

public abstract class RenderSystem {

    private BaseDuctType basicDuctType;
    private List<Player> currentPlayers;

    public RenderSystem(BaseDuctType basicDuctType) {
        this.basicDuctType = basicDuctType;
        this.currentPlayers = Collections.synchronizedList(new ArrayList<>());
    }

    // ***************************************************************
    // ASD UTILS
    // ***************************************************************

    /**
     * creates the ASD information of this duct in respect of its connections and saves it internally.
     * This is only for duct creation, not for duct updates!
     */
    public abstract void createDuctASD(Duct duct, Collection<Direction> connections);

    /**
     * does the same as createDuctASD but for ducts that exist already. The difference in ASD is put into "removeASD" and "addASD" so the caller can send it to the clients.
     */
    public abstract void updateDuctASD(Duct duct, Collection<Direction> connections, List<ArmorStandData> removeASD, List<ArmorStandData> addASD);

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

    public abstract Direction getClickedDuctFace(Player player, Duct duct);

    // ***************************************************************
    //  MISC
    // ***************************************************************

    public List<Player> getCurrentPlayers() {
        return currentPlayers;
    }

    public BaseDuctType getBasicDuctType() {
        return basicDuctType;
    }

}
