package de.robotricker.transportpipes;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.staticutils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.inject.Inject;
import java.util.ArrayList;

public class DuctListener implements Listener {

    @Inject
    private DuctManager ductManager;

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && event.getItem() != null && event.getItem().getType() == Material.STICK) {
            for (BasicDuctType bdt : BasicDuctType.values()) {
                for (DuctType dt : bdt.ductTypeValues()) {
                    event.getPlayer().getInventory().addItem(dt.getItem());
                }
            }
        } else if (clickedBlock != null && event.getItem() != null) {
            DuctType dt = ItemUtils.readDuctNBTTags(event.getItem());
            if (dt != null) {
                Duct duct = dt.getBasicDuctType().createDuct(dt, new BlockLoc(player.getLocation()), player.getLocation().getChunk());
                ductManager.createDuct(duct, new ArrayList<>());
            }
        }
    }
}
