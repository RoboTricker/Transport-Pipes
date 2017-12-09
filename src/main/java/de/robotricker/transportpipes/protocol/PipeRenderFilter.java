package de.robotricker.transportpipes.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.settings.SettingsUtils;

public class PipeRenderFilter {

	private Map<Player, List<Location>> cached_renderedPipeLocs;

	public PipeRenderFilter() {
		cached_renderedPipeLocs = Collections.synchronizedMap(new HashMap<Player, List<Location>>());
	}
	
	public boolean canRenderPipe(Player p, Pipe pipe) {
		return cached_renderedPipeLocs.containsKey(p) ? cached_renderedPipeLocs.get(p).contains(pipe.getBlockLoc()) : false;
	}

	public boolean canRenderItem(Player p, PipeItem item) {
		return cached_renderedPipeLocs.containsKey(p) ? cached_renderedPipeLocs.get(p).contains(item.getBlockLoc()) : false;
	}

	public void doOcclusionCullingCheck(Player p, Pipe pipe) {
		RenderSystem prs = TransportPipes.instance.armorStandProtocol.getPlayerPipeRenderSystem(p);
		AxisAlignedBB aabb = prs.getOuterHitbox(pipe);

		//min
		Vector direction = aabb.getMinLocation(p.getWorld()).subtract(p.getEyeLocation()).toVector();
		Iterator<Block> it = new BlockIterator(p.getWorld(), p.getEyeLocation().toVector(), direction, 0d, TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(p).getRenderDistance());
		while (it.hasNext()) {
			Block b = it.next();
		}

		//max
		direction = aabb.getMaxLocation(p.getWorld()).subtract(p.getEyeLocation()).toVector();
		it = new BlockIterator(p.getWorld(), p.getEyeLocation().toVector(), direction, 0d, TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(p).getRenderDistance());

	}

}
