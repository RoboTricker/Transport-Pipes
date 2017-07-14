package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import de.robotricker.transportpipes.pipeutils.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.transportpipes.manager.saving.SavingManager;
import de.robotricker.transportpipes.manager.settings.SettingsInv;
import de.robotricker.transportpipes.pipes.GoldenPipeInv;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.CraftUtils;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeNeighborBlockUtils;
import de.robotricker.transportpipes.pipeutils.commands.ReloadConfigCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadPipesCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.TPSCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.UpdateCommandExecutor;
import de.robotricker.transportpipes.pipeutils.config.GeneralConf;
import de.robotricker.transportpipes.pipeutils.config.LocConf;
import de.robotricker.transportpipes.pipeutils.hitbox.HitboxListener;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.PipePacketManager;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.utils.ModelledPipeManager;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.utils.VanillaPipeManager;
import de.robotricker.transportpipes.update.UpdateManager;

/**
 * <h1>TransportPipes Spigot/Bukkit Plugin for Minecraft 1.9+</h1>
 * <p>
 * All ThreadSafe Attributes (if you iterate through them, you still have to put the iteration inside of a synchronized(pipes) block):
 * <ul>
 * <li>- TransportPipes#pipes</li>
 * <li>- PipeThread#tickList</li>
 * <li>- Pipe#tempPipeItems</li>
 * <li>- Pipe#tempPipeItemsWithSpawn</li>
 * <li>- Pipe#pipeNeighborBlocks</li>
 * </ul>
 * 
 * @author RoboTricker
 *
 */

public class TransportPipes extends JavaPlugin {

	public String PIPE_NAME;
	public String GOLDEN_PIPE_NAME;
	public String IRON_PIPE_NAME;
	public String ICE_PIPE_NAME;
	public String WRENCH_NAME;

	//TODO private access
	public static TransportPipes instance;
	public static ArmorStandProtocol armorStandProtocol;
	public static PipeThread pipeThread;
	public static PipePacketManager pipePacketManager;
	public static UpdateManager updateManager;

	//x << 34 | y << 26 | z
	private static Map<World, Map<BlockLoc, Pipe>> ppipes;
	public static List<String> antiCheatPlugins;

	public static PipeManager vanillaPipeManager;
	public static PipeManager modelledPipeManager;

	public LocConf locConf;
	public GeneralConf generalConf;

	@Override
	public void onEnable() {
		instance = this;

		// Prepare collections
		ppipes = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Pipe>>());
		antiCheatPlugins = new ArrayList<>();

		// Prepare managers
		armorStandProtocol = new ArmorStandProtocol();
		pipePacketManager = new PipePacketManager();

		locConf = new LocConf();
		generalConf = new GeneralConf();

		//color codes are applied for individual colored pipe names
		PIPE_NAME = locConf.get(LocConf.PIPES_COLORED);
		ICE_PIPE_NAME = "§b" + locConf.get(LocConf.PIPES_ICE);
		GOLDEN_PIPE_NAME = "§6" + locConf.get(LocConf.PIPES_GOLDEN);
		IRON_PIPE_NAME = "§7" + locConf.get(LocConf.PIPES_IRON);
		WRENCH_NAME = "§c" + locConf.get(LocConf.PIPES_WRENCH);

		// Load config
		antiCheatPlugins.addAll(generalConf.getAnticheatPlugins());

		vanillaPipeManager = new VanillaPipeManager(armorStandProtocol);
		modelledPipeManager = new ModelledPipeManager(armorStandProtocol);

		PipeThread.setRunning(true);
		pipeThread = new PipeThread();
		pipeThread.setDaemon(true);
		pipeThread.setPriority(Thread.MIN_PRIORITY);
		pipeThread.start();

		final SettingsInv settingsInv = new SettingsInv();
		final TPSCommandExecutor tpsCmdExec = new TPSCommandExecutor();
		final ReloadConfigCommandExecutor reloadConfigCmdExec = new ReloadConfigCommandExecutor();
		final ReloadPipesCommandExecutor reloadPipesCmdExec = new ReloadPipesCommandExecutor();
		final UpdateCommandExecutor updateCmdExec = new UpdateCommandExecutor();
		final GiveCommandExecutor giveCmdExec = new GiveCommandExecutor();

		getCommand("transportpipes").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

				boolean noPerm = false;

				if (args.length >= 1 && args[0].equalsIgnoreCase("tps")) {
					if (!tpsCmdExec.onCommand(cs)) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("settings")) {
					if (!settingsInv.onCommand(cs)) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("update")) {
					if (!updateCmdExec.onCommand(cs)) {
						noPerm = true;
					}
				} else if (args.length >= 2 && args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("config")) {
					if (!reloadConfigCmdExec.onCommand(cs)) {
						noPerm = true;
					}
				} else if (args.length >= 2 && args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("pipes")) {
					if (!reloadPipesCmdExec.onCommand(cs)) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("give")) {
					if (!giveCmdExec.onCommand(cs)) {
						noPerm = true;
					}
				} else {
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m-----------&7&l[ &6TransportPipes " + TransportPipes.instance.getDescription().getVersion() + "&7&l]&7&l&m-----------"));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes settings &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_SETTINGS)));
					if (cs.hasPermission(generalConf.getPermissionTps()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes tps &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_TPS)));
					if (cs.hasPermission(generalConf.getPermissionReload()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes reload <config|pipes> &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_RELOAD)));
					if (cs.hasPermission(generalConf.getPermissionUpdate()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes update &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_UPDATE)));
					if (cs.hasPermission((generalConf.getPermissionGive())
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes give &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_GIVE)));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m-------------------------------------------"));
					return true;
				}

				if (noPerm) {
					cs.sendMessage(LocConf.load(LocConf.COMMANDS_NOPERM));
				}

				return true;
			}
		});

		Bukkit.getPluginManager().registerEvents(settingsInv, this);
		Bukkit.getPluginManager().registerEvents(new CraftUtils(), this);
		Bukkit.getPluginManager().registerEvents(new GoldenPipeInv(), this);
		Bukkit.getPluginManager().registerEvents(new SavingManager(), this);
		Bukkit.getPluginManager().registerEvents(new PipeNeighborBlockUtils(), this);
		Bukkit.getPluginManager().registerEvents(new HitboxListener(), this);
		Bukkit.getPluginManager().registerEvents(pipePacketManager, this);
		Bukkit.getPluginManager().registerEvents((ModelledPipeManager) modelledPipeManager, this);

		updateManager = new UpdateManager(this);
		if (generalConf.isCheckUpdates()) {
			Bukkit.getPluginManager().registerEvents(updateManager, this);
		}

		for (World world : Bukkit.getWorlds()) {
			SavingManager.loadPipesSync(world);
		}

		CraftUtils.initRecipes();

	}

	@Override
	public void onDisable() {
		PipeThread.setRunning(false);
		try {
			pipeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SavingManager.savePipesSync();
	}

	public static Map<BlockLoc, Pipe> getPipeMap(World world) {
		if (ppipes.containsKey(world)) {
			return ppipes.get(world);
		}
		return null;
	}

	public static void putPipe(final Pipe pipe, final List<PipeDirection> neighborPipes) {
		Map<BlockLoc, Pipe> pipeMap = getPipeMap(pipe.blockLoc.getWorld());
		if (pipeMap == null) {
			pipeMap = Collections.synchronizedMap(new TreeMap<BlockLoc, Pipe>());
			ppipes.put(pipe.blockLoc.getWorld(), pipeMap);
		}
		pipeMap.put(convertBlockLoc(pipe.blockLoc), pipe);

		Bukkit.getScheduler().runTask(instance, new Runnable() {

			@Override
			public void run() {
				//create a list with pipe- and block connections (pipe connections is already given as parameter)
				List<PipeDirection> allConnections = new ArrayList<PipeDirection>();

				List<PipeDirection> neighborBlocks = PipeNeighborBlockUtils.getOnlyNeighborBlocksConnectionsSync(pipe.getBlockLoc());
				allConnections.addAll(neighborBlocks);
				for (PipeDirection neighborPipe : neighborPipes) {
					if (!allConnections.contains(neighborPipe)) {
						allConnections.add(neighborPipe);
					}
				}
				pipe.pipeNeighborBlocks.clear();
				pipe.pipeNeighborBlocks.addAll(neighborBlocks);

				TransportPipes.pipePacketManager.createPipe(pipe, allConnections);
			}
		});
	}

	public static boolean canBuild(Player p, Block b, Block placedAgainst, EquipmentSlot es) {
		BlockBreakEvent bbe = new BlockBreakEvent(b, p);

		//unregister anticheat listeners
		List<RegisteredListener> unregisterListeners = new ArrayList<>();
		for (RegisteredListener rl : bbe.getHandlers().getRegisteredListeners()) {
			for (String antiCheat : antiCheatPlugins) {
				if (rl.getPlugin().getName().equalsIgnoreCase(antiCheat)) {
					unregisterListeners.add(rl);
				}
			}
		}
		for (RegisteredListener rl : unregisterListeners) {
			bbe.getHandlers().unregister(rl);
		}

		Bukkit.getPluginManager().callEvent(bbe);

		//register anticheat listeners
		bbe.getHandlers().registerAll(unregisterListeners);

		return !bbe.isCancelled() || p.isOp();
	}

	public static BlockLoc convertBlockLoc(Location blockLoc) {
		return new BlockLoc(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
	}

	public PipeManager[] getAllPipeManagers() {
		return new PipeManager[] { vanillaPipeManager, modelledPipeManager };
	}

	public int[] convertArmorStandListToEntityIdArray(List<ArmorStandData> ASD) {
		Set<Integer> ids = new HashSet<Integer>();
		if (ASD != null) {
			for (ArmorStandData data : ASD) {
				if (data.getEntityID() != -1) {
					ids.add(data.getEntityID());
				}
			}
		}
		int[] idsArray = new int[ids.size()];
		Iterator<Integer> it = ids.iterator();
		for (int i = 0; it.hasNext(); i++) {
			idsArray[i] = it.next();
		}
		return idsArray;
	}

	public static class BlockLoc implements Comparable<BlockLoc> {

		private int x;
		private int y;
		private int z;

		public BlockLoc(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof BlockLoc)) {
				return false;
			}
			BlockLoc bl = (BlockLoc) obj;
			return bl.x == x && bl.y == y && bl.z == z;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y, z);
		}

		@Override
		public int compareTo(BlockLoc o) {
			if (z < o.z) {
				return -1;
			} else if (z > o.z) {
				return 1;
			} else {
				if (y < o.y) {
					return -1;
				} else if (y > o.y) {
					return 1;
				} else {
					if (x < o.x) {
						return -1;
					} else if (x > o.x) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		}

	}

}
