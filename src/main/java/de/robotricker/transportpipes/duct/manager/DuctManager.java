package de.robotricker.transportpipes.duct.manager;

import org.bukkit.entity.Player;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.protocol.ProtocolService;

public abstract class DuctManager<T extends Duct> {

    protected TransportPipes transportPipes;
    protected DuctRegister ductRegister;
    protected GlobalDuctManager globalDuctManager;
    protected ProtocolService protocolService;
    protected ItemService itemService;

    @Inject
    public DuctManager(TransportPipes transportPipes, DuctRegister ductRegister, GlobalDuctManager globalDuctManager, ProtocolService protocolService, ItemService itemService) {
        this.transportPipes = transportPipes;
        this.ductRegister = ductRegister;
        this.globalDuctManager = globalDuctManager;
        this.protocolService = protocolService;
        this.itemService = itemService;
    }

    public abstract void registerDuctTypes();

    public abstract void registerRecipes();

    public abstract void tick();

    /**
     * called inside the bukkit thread whenever a duct comes into visible range
     */
    public void notifyDuctShown(Duct duct, Player p) {
        if (globalDuctManager.getPlayerDucts(p).add(duct)) {
            protocolService.sendASD(p, duct.getBlockLoc(), globalDuctManager.getPlayerRenderSystem(p, duct.getDuctType().getBaseDuctType()).getASDForDuct(duct));
        }
    }

    /**
     * called inside the bukkit thread whenever a duct gets outside of the visible range
     */
    public void notifyDuctHidden(Duct duct, Player p) {
        if (globalDuctManager.getPlayerDucts(p).remove(duct)) {
            protocolService.removeASD(p, globalDuctManager.getPlayerRenderSystem(p, duct.getDuctType().getBaseDuctType()).getASDForDuct(duct));
        }
    }

    public void updateNonDuctConnections(Duct duct) {

    }

}
