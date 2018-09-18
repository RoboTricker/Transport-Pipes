package de.robotricker.transportpipes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.staticutils.ItemUtils;

public class DuctListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock != null && e.getItem() != null && e.getItem().getType() == Material.STICK) {
            for (BasicDuctType bdt : BasicDuctType.values()) {
                for (DuctType dt : bdt.ductTypeValues()) {
                    e.getPlayer().getInventory().addItem(dt.getItem());
                }
            }
        } else if (clickedBlock != null && e.getItem() != null) {
            DuctType dt = ItemUtils.readDuctNBTTags(e.getItem());
            if (dt != null) {
                Duct duct = dt.getBasicDuctType().createDuct(dt, new BlockLoc(p.getLocation()), p.getLocation().getChunk());
                TransportPipes.instance.getDuctManager().createDuct(duct, new ArrayList<>());
            }
        }
    }

}
