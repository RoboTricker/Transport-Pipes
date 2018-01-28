package de.robotricker.transportpipes.utils.staticutils;

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

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.flowpowered.nbt.stream.NBTOutputStream;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;

public class SavingUtils implements Listener {

	private List<World> loadedWorlds;
	private boolean saving;
	private boolean loading;

	public SavingUtils() {
		loadedWorlds = new ArrayList<>();
		saving = false;
		loading = false;
	}

	public void saveDuctsAsync(final boolean message) {
		TransportPipes.runTaskAsynchronously(new Runnable() {

			@Override
			public void run() {
				saveDuctsSync(message);
			}
		});
	}

	public void saveDuctsSync(boolean message) {
		if (saving) {
			return;
		}
		saving = true;
		int ductsCount = 0;
		try {
			HashMap<World, List<CompoundMap>> worlds = new HashMap<>();

			// cache worlds
			for (World world : Bukkit.getWorlds()) {
				List<CompoundMap> ductList = new ArrayList<>();
				worlds.put(world, ductList);

				// put ducts in tag lists
				Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
				if (ductMap != null) {
					synchronized (ductMap) {
						for (Duct duct : ductMap.values()) {
							// save individual ducts
							CompoundMap tags = new CompoundMap();
							duct.saveToNBTTag(tags);
							ductList.add(tags);
							ductsCount++;
						}
					}
				}

			}

			// save tag lists to files
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

					NBTOutputStream out = new NBTOutputStream(new FileOutputStream(datFile), true);

					CompoundMap tags = new CompoundMap();

					NBTUtils.saveStringValue(tags, "PluginVersion", TransportPipes.instance.getDescription().getVersion());
					NBTUtils.saveLongValue(tags, "LastSave", System.currentTimeMillis());

					List<CompoundMap> rawDuctList = worlds.get(world);
					List<Tag<?>> finalDuctList = new ArrayList<>();
					for (CompoundMap map : rawDuctList) {
						finalDuctList.add(new CompoundTag("Duct", map));
					}
					NBTUtils.saveListValue(tags, "Ducts", CompoundTag.class, finalDuctList);

					CompoundTag compound = new CompoundTag("Data", tags);
					out.writeTag(compound);
					out.close();
				} catch (FileNotFoundException ignored) {

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (message) {
			TransportPipes.instance.getLogger().info("saved " + ductsCount + " ducts in " + Bukkit.getWorlds().size() + " worlds");
		}
		saving = false;
	}

	/**
	 * loads all ducts and optional pipe items in this world if it isn't loaded
	 * already
	 */
	public void loadDuctsSync(final World world) {
		if (!loadedWorlds.contains(world)) {
			loadedWorlds.add(world);
		} else {
			return;
		}
		loading = true;
		try {
			int ductsCount = 0;

			File datFile = new File(Bukkit.getWorldContainer(), world.getName() + "/pipes.dat");
			if (!datFile.exists()) {
				return;
			}

			CompoundTag compound = null;
			NBTInputStream in = null;

			try {
				in = new NBTInputStream(new FileInputStream(datFile), true);
				compound = (CompoundTag) in.readTag();
			} catch (EOFException | ZipException e) {
				TransportPipes.instance.getLogger().info("Wrong pipes.dat version detected. Converting to new nbt version");
				in = new NBTInputStream(new FileInputStream(datFile), false);
				compound = (CompoundTag) in.readTag();
			}

			String pipesDatVersionString = NBTUtils.readStringTag(compound.getValue().get("PluginVersion"), TransportPipes.instance.getDescription().getVersion());
			long pipesDatVersion = UpdateUtils.convertVersionToLong(pipesDatVersionString);

			if (pipesDatVersion <= UpdateUtils.convertVersionToLong("3.8.22")) {

				List<Tag<?>> ductList = NBTUtils.readListTag(compound.getValue().get("Pipes"));

				for (Tag<?> tag : ductList) {
					CompoundTag pipeTag = (CompoundTag) tag;

					PipeType pt = PipeType.getFromId(NBTUtils.readIntTag(pipeTag.getValue().get("PipeType"), PipeType.COLORED.getId()));
					Location pipeLoc = LocationUtils.StringToLoc(NBTUtils.readStringTag(pipeTag.getValue().get("PipeLocation"), null));

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

					List<WrappedDirection> neighborPipes = new ArrayList<WrappedDirection>();
					List<Tag<?>> neighborPipesList = NBTUtils.readListTag(pipeTag.getValue().get("NeighborPipes"));
					for (Tag<?> neighborPipesEntry : neighborPipesList) {
						neighborPipes.add(WrappedDirection.fromID(NBTUtils.readIntTag(neighborPipesEntry, 0)));
					}

					PipeDetails pipeDetails = new PipeDetails(pt);
					if (pt == PipeType.COLORED) {
						pipeDetails.setPipeColor(PipeColor.WHITE);
					}

					if (pipeLoc != null) {
						Pipe pipe = (Pipe) pipeDetails.createDuct(pipeLoc);

						pipe.loadFromNBTTag(pipeTag, pipesDatVersion);

						// save and spawn pipe
						DuctUtils.registerDuct(pipe, neighborPipes);

						ductsCount++;
					}

				}

				TransportPipes.instance.pipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						// update all pipes connections because the in the old system "NeighborPipes"
						// wasn't saved for each pipe
						Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
						if (ductMap != null) {
							synchronized (ductMap) {
								for (Duct duct : ductMap.values()) {
									TransportPipes.instance.ductManager.updateDuct(duct);
								}
							}
						}
					};

				}, 2);
			} else if (pipesDatVersion < UpdateUtils.convertVersionToLong("4.3.0")) {

				List<Tag<?>> ductList = NBTUtils.readListTag(compound.getValue().get("Pipes"));

				for (Tag<?> tag : ductList) {
					CompoundTag pipeTag = (CompoundTag) tag;

					PipeType pt = PipeType.getFromId(NBTUtils.readIntTag(pipeTag.getValue().get("PipeType"), PipeType.COLORED.getId()));
					Location pipeLoc = LocationUtils.StringToLoc(NBTUtils.readStringTag(pipeTag.getValue().get("PipeLocation"), null));

					List<WrappedDirection> neighborPipes = new ArrayList<WrappedDirection>();
					List<Tag<?>> neighborPipesList = NBTUtils.readListTag(pipeTag.getValue().get("NeighborPipes"));
					for (Tag<?> neighborPipesEntry : neighborPipesList) {
						neighborPipes.add(WrappedDirection.fromID(NBTUtils.readIntTag(neighborPipesEntry, 0)));
					}

					PipeDetails pipeDetails = new PipeDetails(pt);
					if (pt == PipeType.COLORED) {
						pipeDetails.setPipeColor(PipeColor.WHITE);
					}

					if (pipeLoc != null) {
						Pipe pipe = (Pipe) pipeDetails.createDuct(pipeLoc);

						pipe.loadFromNBTTag(pipeTag, pipesDatVersion);

						// save and spawn pipe
						DuctUtils.registerDuct(pipe, neighborPipes);

						ductsCount++;
					}

				}

			} else {
				List<Tag<?>> ductList = NBTUtils.readListTag(compound.getValue().get("Ducts"));
				for (Tag<?> tag : ductList) {
					CompoundTag ductTag = (CompoundTag) tag;

					DuctType ductType = DuctType.valueOf(NBTUtils.readStringTag(ductTag.getValue().get("DuctType"), null));
					String serializedDuctDetails = NBTUtils.readStringTag(ductTag.getValue().get("DuctDetails"), null);
					DuctDetails ductDetails = ductType.createDuctDetails(serializedDuctDetails);
					Location ductLoc = LocationUtils.StringToLoc(NBTUtils.readStringTag(ductTag.getValue().get("DuctLocation"), null));

					List<WrappedDirection> neighborDucts = new ArrayList<WrappedDirection>();
					List<Tag<?>> neighborDuctsList = NBTUtils.readListTag(ductTag.getValue().get("NeighborDucts"));
					for (Tag<?> neighborDuctsEntry : neighborDuctsList) {
						neighborDucts.add(WrappedDirection.fromID(NBTUtils.readIntTag(neighborDuctsEntry, 0)));
					}

					Duct duct = ductDetails.createDuct(ductLoc);
					duct.loadFromNBTTag(ductTag, pipesDatVersion);

					// save and spawn duct
					DuctUtils.registerDuct(duct, neighborDucts);

					ductsCount++;
				}
			}

			in.close();

			TransportPipes.instance.getLogger().info(ductsCount + " ducts loaded in world " + world.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}

		loading = false;

	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) {
		// only save once for all worlds
		if (e.getWorld().equals(Bukkit.getWorlds().get(0))) {
			saveDuctsAsync(false);
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		loadDuctsSync(e.getWorld());
	}

}
