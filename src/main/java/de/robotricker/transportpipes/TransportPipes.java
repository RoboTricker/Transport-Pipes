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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.transportpipes.manager.saving.SavingManager;
import de.robotricker.transportpipes.manager.settings.SettingsInv;
import de.robotricker.transportpipes.pipes.GoldenPipeInv;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.CraftUtils;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeNeighborBlockUtils;
import de.robotricker.transportpipes.pipeutils.ProtocolUtils;
import de.robotricker.transportpipes.pipeutils.commands.ReloadConfigCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadPipesCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.TPSCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.UpdateCommandExecutor;
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

	public String PREFIX;

	public String PIPE_NAME;
	public String GOLDEN_PIPE_NAME;
	public String IRON_PIPE_NAME;
	public String ICE_PIPE_NAME;
	public String WRENCH_NAME;

	// TODO: private access
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

	@Override
	public void onEnable() {
		instance = this;

		// Prepare collections
		ppipes = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Pipe>>());
		antiCheatPlugins = new ArrayList<>();

		// Prepare managers
		armorStandProtocol = new ArmorStandProtocol();
		pipePacketManager = new PipePacketManager();

		// Load config and update values
		getConfig().options().copyDefaults(true);
		if (getConfig().getString("pipename.pipe").startsWith("&f")) {
			getConfig().set("pipename.pipe", getConfig().getString("pipename.pipe").substring(2));
		}
		if (getConfig().getString("pipename.golden_pipe").startsWith("&6")) {
			getConfig().set("pipename.golden_pipe", getConfig().getString("pipename.golden_pipe").substring(2));
		}
		if (getConfig().getString("pipename.iron_pipe").startsWith("&7")) {
			getConfig().set("pipename.iron_pipe", getConfig().getString("pipename.iron_pipe").substring(2));
		}
		if (getConfig().getString("pipename.wrench").startsWith("&c")) {
			getConfig().set("pipename.wrench", getConfig().getString("pipename.wrench").substring(2));
		}
		if (!getConfig().getString("prefix").contains("&")) {
			getConfig().set("prefix", "&7[&6TransportPipes&7] &6");
		}
		saveConfig();

		// Load language data
		PREFIX = getFormattedConfigString("prefix");
		PIPE_NAME = getFormattedConfigString("pipename.pipe");
		GOLDEN_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&6" + getConfig().getString("pipename.golden_pipe"));
		IRON_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&7" + getConfig().getString("pipename.iron_pipe"));
		ICE_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&b" + getConfig().getString("pipename.ice_pipe"));
		WRENCH_NAME = ChatColor.translateAlternateColorCodes('&', "&c" + getConfig().getString("pipename.wrench"));

		// Load config
		antiCheatPlugins.addAll(getConfig().getStringList("anticheat"));

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
				} else {
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m-----------&7&l[ &6TransportPipes " + TransportPipes.instance.getDescription().getVersion() + "&7&l]&7&l&m-----------"));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes settings &7- &bOpens a settings menu in which you can change the render distance of the pipes."));
					if (cs.hasPermission(getConfig().getString("permissions.tps", "tp.tps")))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes tps &7- &bShows some general information about the pipes in all worlds and the ticks per second of the plugin thread."));
					if (cs.hasPermission(getConfig().getString("permissions.reload", "tp.reload")))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes reload <config|pipes> &7- &bReloads all pipes or the config."));
					if (cs.hasPermission(getConfig().getString("permissions.update", "tp.update")))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes update &7- &bChecks for a new plugin version at SpigotMC and updates the plugin if possible."));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m-------------------------------------------"));
					return true;
				}

				if (noPerm) {
					cs.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
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
		if (getConfig().getBoolean("check_updates_onjoin")) {
			Bukkit.getPluginManager().registerEvents(updateManager, this);
		}

		for (World world : Bukkit.getWorlds()) {
			SavingManager.loadPipesSync(world);
		}

		CraftUtils.initRecipes();

		ProtocolUtils.init();

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

	public static String getFormattedConfigString(String key) {
		return ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString(key));
	}

	public ItemStack getPipeItemForPlayer2(Player p, PipeType pt, PipeColor pc) {
		return armorStandProtocol.getPlayerPipeManager(p).getPipeItem(pt, pc);
	}

	public ItemStack getWrenchItemForPlayer2(Player p) {
		return armorStandProtocol.getPlayerPipeManager(p).getWrenchItem();
	}

	public ItemStack getVanillaPipeItem(PipeType pt, PipeColor pc) {
		return vanillaPipeManager.getPipeItem(pt, pc);
	}

	public ItemStack getVanillaWrenchItem() {
		return vanillaPipeManager.getWrenchItem();
	}

	public static boolean isItemStackWrench(ItemStack is) {
		if (is == null) {
			return false;
		}
		if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
			if (is.getItemMeta().getDisplayName().equals(instance.WRENCH_NAME)) {
				return true;
			}
		}
		return false;
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

	public static ItemStack replaceVanillaWithModelledItemStack(ItemStack before) {
		if (before == null || before.getType() == Material.AIR) {
			return before;
		}
		for (PipeType pt : PipeType.values()) {
			if (pt == PipeType.COLORED) {
				for (PipeColor pc : PipeColor.values()) {
					ItemStack modelledIs = modelledPipeManager.getPipeItem(pt, pc);
					ItemStack vanillaId = vanillaPipeManager.getPipeItem(pt, pc);
					if (before.isSimilar(vanillaId)) {
						ItemStack returnedIs = modelledIs.clone();
						returnedIs.setAmount(before.getAmount());
						return returnedIs;
					}
				}
			} else {
				ItemStack modelledIs = modelledPipeManager.getPipeItem(pt, null);
				ItemStack vanillaId = vanillaPipeManager.getPipeItem(pt, null);
				if (before.isSimilar(vanillaId)) {
					ItemStack returnedIs = modelledIs.clone();
					returnedIs.setAmount(before.getAmount());
					return returnedIs;
				}
			}
		}
		ItemStack modelledWrenchIs = modelledPipeManager.getWrenchItem();
		ItemStack vanillaWrenchIs = vanillaPipeManager.getWrenchItem();
		if (before.isSimilar(vanillaWrenchIs)) {
			ItemStack returnedIs = modelledWrenchIs.clone();
			returnedIs.setAmount(before.getAmount());
			return returnedIs;
		}
		return before;
	}

	public static ItemStack replaceModelledWithVanillaItemStack(ItemStack before) {
		if (before == null || before.getType() == Material.AIR) {
			return before;
		}
		for (PipeType pt : PipeType.values()) {
			if (pt == PipeType.COLORED) {
				for (PipeColor pc : PipeColor.values()) {
					ItemStack modelledIs = modelledPipeManager.getPipeItem(pt, pc);
					ItemStack vanillaId = vanillaPipeManager.getPipeItem(pt, pc);
					if (before.isSimilar(modelledIs)) {
						ItemStack returnedIs = vanillaId.clone();
						returnedIs.setAmount(before.getAmount());
						return returnedIs;
					}
				}
			} else {
				ItemStack modelledIs = modelledPipeManager.getPipeItem(pt, null);
				ItemStack vanillaId = vanillaPipeManager.getPipeItem(pt, null);
				if (before.isSimilar(modelledIs)) {
					ItemStack returnedIs = vanillaId.clone();
					returnedIs.setAmount(before.getAmount());
					return returnedIs;
				}
			}
		}
		ItemStack modelledWrenchIs = modelledPipeManager.getWrenchItem();
		ItemStack vanillaWrenchIs = vanillaPipeManager.getWrenchItem();
		if (before.isSimilar(modelledWrenchIs)) {
			ItemStack returnedIs = vanillaWrenchIs.clone();
			returnedIs.setAmount(before.getAmount());
			return returnedIs;
		}
		return before;
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
