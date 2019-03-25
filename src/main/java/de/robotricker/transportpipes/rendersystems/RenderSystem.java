package de.robotricker.transportpipes.rendersystems;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public abstract class RenderSystem {

    private BaseDuctType baseDuctType;

    public RenderSystem(BaseDuctType baseDuctType) {
        this.baseDuctType = baseDuctType;
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
     * does the same as createDuctASD but for duct that exist already. The difference in ASD is put into "removeASD" and "addASD" so the caller can send it to the clients.
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

    public BaseDuctType getBaseDuctType() {
        return baseDuctType;
    }

    public static ItemStack getItem(String renderSystemDisplayName, ItemService itemService, DuctRegister ductRegister) {
        if (ModelledRenderSystem.getDisplayName().equalsIgnoreCase(renderSystemDisplayName)) {
            return ModelledRenderSystem.getItem(itemService);
        }
        if (VanillaRenderSystem.getDisplayName().equalsIgnoreCase(renderSystemDisplayName)) {
            return VanillaRenderSystem.getItem(ductRegister);
        }
        return null;
    }

    public static RenderSystem getRenderSystem(String renderSystemName, BaseDuctType baseDuctType) {
        if (baseDuctType.getVanillaRenderSystem() != null && VanillaRenderSystem.getDisplayName().equalsIgnoreCase(renderSystemName)) {
            return baseDuctType.getVanillaRenderSystem();
        }
        if (baseDuctType.getModelledRenderSystem() != null && ModelledRenderSystem.getDisplayName().equalsIgnoreCase(renderSystemName)) {
            return baseDuctType.getModelledRenderSystem();
        }
        return null;
    }

    public static String getLocalizedRenderSystemName(String renderSystemName) {
        if (renderSystemName.equalsIgnoreCase("modelled")) {
            return LangConf.Key.RENDERSYSTEM_NAME_MODELLED.get();
        } else if (renderSystemName.equalsIgnoreCase("vanilla")) {
            return LangConf.Key.RENDERSYSTEM_NAME_VANILLA.get();
        }
        return null;
    }

}
