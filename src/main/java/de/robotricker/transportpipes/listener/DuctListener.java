package de.robotricker.transportpipes.listener;

import de.robotricker.transportpipes.DuctManager;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.util.ItemUtils;
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
            for (BaseDuctType ductBaseType : BaseDuctType.values()) {
                for (DuctType ductType : ductBaseType.ductTypeValues()) {
                    event.getPlayer().getInventory().addItem(ductType.getItem());
                }
            }
        } else if (clickedBlock != null && event.getItem() != null) {
            DuctType ductType = ItemUtils.readDuctNBTTags(event.getItem());
            if (ductType != null) {
                Duct duct = ductType.getBasicDuctType().createDuct(ductType, new BlockLocation(player.getLocation()), player.getLocation().getChunk());
                ductManager.createDuct(duct, new ArrayList<>());
            }
        }
    }
}
