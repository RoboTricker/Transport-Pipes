package de.robotricker.transportpipes.saving;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.jnbt.CompoundTag;
import org.jnbt.NBTCompression;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.NBTTagType;
import org.jnbt.Tag;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.update.UpdateManager;

public class SavingManager implements Listener {

	private static List<World> loadedWorlds = new ArrayList<>();
	private static boolean saving = false;

	public static void savePipesAsync() {
		Bukkit.getScheduler().runTaskAsynchronously(TransportPipes.instance, new Runnable() {

			@Override
			public void run() {
				savePipesSync();
			}
		});
	}

	public static void savePipesSync() {
		if (saving) {
			return;
		}
		saving = true;
		int pipesCount = 0;
		try {
			HashMap<World, List<HashMap<String, Tag>>> worlds = new HashMap<>();

			//cache worlds
			for (World world : Bukkit.getWorlds()) {
				List<HashMap<String, Tag>> pipeList = new ArrayList<>();
				worlds.put(world, pipeList);

				//put pipes in Tag Lists
				Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
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

					if (datFile.exists()) {
						// Security for delete old fail on saving system.
						if (datFile.isDirectory()) {
							datFile.delete();
							datFile.createNewFile();
						}
						// Security end
					} else {
						datFile.createNewFile();
					}

					NBTOutputStream out = new NBTOutputStream(new FileOutputStream(datFile), NBTCompression.GZIP);

					HashMap<String, Tag> tags = new HashMap<>();

					NBTUtils.saveStringValue(tags, "PluginVersion", TransportPipes.instance.getDescription().getVersion());
					NBTUtils.saveLongValue(tags, "LastSave", System.currentTimeMillis());

					List<HashMap<String, Tag>> rawPipeList = worlds.get(world);
					List<Tag> finalPipeList = new ArrayList<>();
					for (HashMap<String, Tag> map : rawPipeList) {
						finalPipeList.add(new CompoundTag("Pipe", map));
					}
					NBTUtils.saveListValue(tags, "Pipes", NBTTagType.TAG_COMPOUND, finalPipeList);

					CompoundTag compound = new CompoundTag("Data", tags);
					out.writeTag(compound);
					out.close();
				} catch (FileNotFoundException ignored) {
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[TransportPipes] saved " + pipesCount + " pipes in " + Bukkit.getWorlds().size() + " worlds");
		saving = false;
	}

	/**
	 * loads all pipes and items in this world if it isn't loaded already
	 */
	public static void loadPipesSync(final World world) {

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

			CompoundTag compound = null;
			NBTInputStream in = null;

			try {
				in = new NBTInputStream(new FileInputStream(datFile), NBTCompression.GZIP);
				compound = (CompoundTag) in.readTag();
			} catch (EOFException | ZipException e) {
				System.out.println("Wrong pipes.dat version detected. Converting to new nbt version");
				in = new NBTInputStream(new FileInputStream(datFile), NBTCompression.UNCOMPRESSED);
				compound = (CompoundTag) in.readTag();
			}

			//whether to convert the pipes.dat system to a version after 3.8.22 without PipeClassName but instead PipeType values
			boolean convertSystem = false;
			String pipesDatVersionString = NBTUtils.readStringTag(compound.getValue().get("PluginVersion"), TransportPipes.instance.getDescription().getVersion());
			long pipesDatVersion = UpdateManager.convertVersionToLong(pipesDatVersionString);
			if (pipesDatVersion <= UpdateManager.convertVersionToLong("3.8.22")) {
				convertSystem = true;
				System.out.println("Converting pipes.dat system to new system");
			}

			List<Tag> pipeList = NBTUtils.readListTag(compound.getValue().get("Pipes"));

			for (Tag tag : pipeList) {
				CompoundTag pipeTag = (CompoundTag) tag;

				PipeType pt = PipeType.getFromId(NBTUtils.readIntTag(pipeTag.getValue().get("PipeType"), PipeType.COLORED.getId()));
				Location pipeLoc = PipeUtils.StringToLoc(NBTUtils.readStringTag(pipeTag.getValue().get("PipeLocation"), null));

				if (convertSystem) {
					String oldPipeClassName = NBTUtils.readStringTag(pipeTag.getValue().get("PipeClassName"), "de.robotricker.transportpipes.pipes.PipeMID");
					if (oldPipeClassName.endsWith("GoldenPipe")) {
						pt = PipeType.GOLDEN;
					} else if (oldPipeClassName.endsWith("IronPipe")) {
						pt = PipeType.IRON;
					}

					boolean icePipe = NBTUtils.readByteTag(pipeTag.getValue().get("IcePipe"), (byte) 0) == (byte) 1;
					if (icePipe) {
						pt = PipeType.ICE;
					}
				}

				List<PipeDirection> neighborPipes = new ArrayList<PipeDirection>();
				List<Tag> neighborPipesList = NBTUtils.readListTag(pipeTag.getValue().get("NeighborPipes"));
				for (Tag neighborPipesEntry : neighborPipesList) {
					neighborPipes.add(PipeDirection.fromID(NBTUtils.readIntTag(neighborPipesEntry, 0)));
				}

				if (pipeLoc != null) {
					Pipe pipe = pt.createPipe(pipeLoc, PipeColor.WHITE); //PipeColor will be replaced when loading from NBT inside pipe
					pipe.loadFromNBTTag(pipeTag);

					//save and spawn pipe
					PipeUtils.registerPipe(pipe, neighborPipes);

					pipesCount++;
				}

			}

			if (convertSystem) {
				PipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						//update all pipes connections because the in the old system "NeighborPipes" wasn't saved for each pipe
						Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
						if (pipeMap != null) {
							synchronized (pipeMap) {
								for (Pipe pipe : pipeMap.values()) {
									TransportPipes.pipePacketManager.updatePipe(pipe);
								}
							}
						}
					};

				}, 2);
			}

			in.close();

			System.out.println("[TransportPipes] " + pipesCount + " pipes loaded in world " + world.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) {
		//only save once for all worlds
		if (e.getWorld().equals(Bukkit.getWorlds().get(0))) {
			savePipesAsync();
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		loadPipesSync(e.getWorld());
	}

}
