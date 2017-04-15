package de.robotricker.transportpipes.manager.saving;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.LongTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeUtils;

public class SavingManager implements Listener {

	private static List<World> loadedWorlds = new ArrayList<>();

	public static void savePipesSync() {
		int pipesCount = 0;
		try {
			HashMap<World, List<HashMap<String, Tag>>> worlds = new HashMap<>();

			//cache worlds
			for (World world : Bukkit.getWorlds()) {
				List<HashMap<String, Tag>> pipeList = new ArrayList<>();
				worlds.put(world, pipeList);

				//put pipes in Tag Lists
				Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(world);
				if (pipeMap != null) {
					synchronized (pipeMap) {
						for (Pipe pipe : pipeMap.values()) {
							//save individual pipe
							HashMap<String, Tag> tags = new HashMap<>();
							pipe.saveToNBTTag(tags);
							pipeList.add(tags);
							pipesCount++;
						}
					}
				}

			}

			//save Tag Lists to files
			for (World world : worlds.keySet()) {
				try {
					File datFile = new File(Bukkit.getWorldContainer(), world.getName() + "/pipes.dat");
					if (!datFile.exists()) {
						//datFile.mkdirs();
						datFile.createNewFile();
					}
					NBTOutputStream out = new NBTOutputStream(new FileOutputStream(datFile));

					HashMap<String, Tag> tags = new HashMap<>();

					tags.put("PluginVersion", new StringTag("PluginVersion", TransportPipes.instance.getDescription().getVersion()));
					tags.put("LastSave", new LongTag("LastSave", System.currentTimeMillis()));

					List<HashMap<String, Tag>> rawPipeList = worlds.get(world);
					List<Tag> finalPipeList = new ArrayList<>();
					for (HashMap<String, Tag> map : rawPipeList) {
						finalPipeList.add(new CompoundTag("Pipe", map));
					}
					tags.put("Pipes", new ListTag("Pipes", CompoundTag.class, finalPipeList));

					CompoundTag compound = new CompoundTag("Data", tags);
					out.writeTag(compound);
					out.close();
				} catch (FileNotFoundException e) {
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[TransportPipes] saved " + pipesCount + " pipes in " + Bukkit.getWorlds().size() + " worlds");
	}

	/**
	 * loads all pipes and items in this world if it isn't loaded already
	 */
	public static void loadPipesSync(World world) {

		if (!loadedWorlds.contains(world)) {
			loadedWorlds.add(world);
		} else {
			return;
		}

		try {

			int pipesCount = 0;

			File datFile = new File(Bukkit.getWorldContainer(), world.getName() + "/pipes.dat");
			
			if (!datFile.exists()) {
				return;
			}
			
			NBTInputStream in = new NBTInputStream(new FileInputStream(datFile));

			CompoundTag compound = (CompoundTag) in.readTag();

			//String pluginVersion = ((StringTag) compound.getValue().get("PluginVersion")).getValue();
			//long lastSave = ((LongTag) compound.getValue().get("LastSave")).getValue();
			List<Tag> pipeList = ((ListTag) compound.getValue().get("Pipes")).getValue();

			for (Tag tag : pipeList) {
				CompoundTag pipeTag = (CompoundTag) tag;

				String className = ((StringTag) pipeTag.getValue().get("PipeClassName")).getValue();
				Location pipeLoc = PipeUtils.StringToLoc(((StringTag) pipeTag.getValue().get("PipeLocation")).getValue());

				if (pipeLoc != null) {
					Pipe pipe = (Pipe) Class.forName(className).getConstructor(Location.class, List.class).newInstance(pipeLoc, PipeUtils.getPipeNeighborBlocksSync(pipeLoc));
					pipe.loadFromNBTTag(pipeTag);

					//load and spawn pipe
					TransportPipes.putPipe(pipe);
					TransportPipes.pipePacketManager.spawnPipeSync(pipe);

					pipesCount++;
				}

			}

			in.close();

			System.out.println("[TransportPipes] " + pipesCount + " pipes loaded in world " + world.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) {
		if (e.getWorld().equals(Bukkit.getWorlds().get(0))) {
			savePipesSync();
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		loadPipesSync(e.getWorld());
	}

}