package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class IronPipe extends Pipe {

    private TPDirection currentOutputDirection;

    public IronPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager);
        currentOutputDirection = TPDirection.UP;
    }

    @Override
    public void notifyClick(Player p, TPDirection face, boolean shift) {
        super.notifyClick(p, face, shift);
        cycleOutputDirection();
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    public TPDirection getCurrentOutputDirection() {
        return currentOutputDirection;
    }

    private void cycleOutputDirection() {
        Set<TPDirection> allConns = getAllConnections();
        if (allConns.isEmpty()) {
            return;
        }

        TPDirection oldOutputDirection = currentOutputDirection;
        int dirId;
        do {
            dirId = currentOutputDirection.ordinal();
            dirId++;
            dirId %= TPDirection.values().length;
            currentOutputDirection = TPDirection.values()[dirId];
        } while (!allConns.contains(currentOutputDirection));

        if (oldOutputDirection != currentOutputDirection) {
            globalDuctManager.updateDuct(this);
        }

    }

    @Override
    public int[] getBreakParticleData() {
        return new int[] { 42, 0 };
    }

    @Override
    public void notifyConnectionChange() {
        super.notifyConnectionChange();
        Set<TPDirection> allConns = getAllConnections();
        if (!allConns.isEmpty() && !allConns.contains(currentOutputDirection)) {
            cycleOutputDirection();
        }
    }
}
