package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.transportpipes.manager.saving.SavingManager;
import de.robotricker.transportpipes.manager.settings.SettingsInv;
import de.robotricker.transportpipes.pipes.GoldenPipeInv;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.CraftUtils;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeNeighborBlockListener;
import de.robotricker.transportpipes.pipeutils.commands.ReloadConfigCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadPipesCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.TPSCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.UpdateCommandExecutor;
import de.robotricker.transportpipes.pipeutils.hitbox.HitboxListener;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.PipePacketManager;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeGOLDENModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeIRONModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeNORMALModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeEWModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeGOLDENModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeIRONModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeMIDModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeNSModel;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.VanillaPipeUDModel;
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
	public static ItemStack PIPE_ITEM;
	public String GOLDEN_PIPE_NAME;
	public static ItemStack GOLDEN_PIPE_ITEM;
	public String IRON_PIPE_NAME;
	public static ItemStack IRON_PIPE_ITEM;
	public String ICE_PIPE_NAME;
	public static ItemStack ICE_PIPE_ITEM;
	public String WRENCH_NAME;
	public static ItemStack WRENCH_ITEM;

	//x << 34 | y << 26 | z
	public static Map<World, Map<BlockLoc, Pipe>> ppipes = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Pipe>>());

	public static TransportPipes instance;

	public static ArmorStandProtocol armorStandProtocol;
	public static PipeThread pipeThread;
	public static PipePacketManager pipePacketManager;
	public static UpdateManager updateManager;

	public static List<String> antiCheatPlugins = new ArrayList<String>();

	public static ModelledPipeModel modelledNormalModel = new ModelledPipeNORMALModel();
	public static ModelledPipeModel modelledIronModel = new ModelledPipeIRONModel();
	public static ModelledPipeModel modelledGoldenModel = new ModelledPipeGOLDENModel();
	public static VanillaPipeModel vanillaEwModel = new VanillaPipeEWModel();
	public static VanillaPipeModel vanillaNsModel = new VanillaPipeNSModel();
	public static VanillaPipeModel vanillaUdModel = new VanillaPipeUDModel();
	public static VanillaPipeModel vanillaMidModel = new VanillaPipeMIDModel();
	public static VanillaPipeModel vanillaGoldenModel = new VanillaPipeGOLDENModel();
	public static VanillaPipeModel vanillaIronModel = new VanillaPipeIRONModel();

	@Override
	public void onEnable() {
		instance = this;
		armorStandProtocol = new ArmorStandProtocol();
		pipePacketManager = new PipePacketManager();

		getConfig().options().copyDefaults(true);
		saveConfig();

		//version fix
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

		PREFIX = getFormattedConfigString("prefix");
		PIPE_NAME = getFormattedConfigString("pipename.pipe");
		GOLDEN_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&6" + getConfig().getString("pipename.golden_pipe"));
		IRON_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&7" + getConfig().getString("pipename.iron_pipe"));
		ICE_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&b" + getConfig().getString("pipename.ice_pipe"));
		WRENCH_NAME = ChatColor.translateAlternateColorCodes('&', "&c" + getConfig().getString("pipename.wrench"));

		antiCheatPlugins.clear();
		antiCheatPlugins.addAll(getConfig().getStringList("anticheat"));

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
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m---------------&7&l[ &6TransportPipes " + TransportPipes.instance.getDescription().getVersion() + "&7&l]&7&l&m---------------"));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes settings &7- &bOpens a settings menu in which you can change the render distance of the pipes."));
					if (cs.hasPermission(getConfig().getString("permissions.tps", "tp.tps")))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes tps &7- &bShows some general information about the pipes in all worlds and the ticks per second of the plugin thread."));
					if (cs.hasPermission(getConfig().getString("permissions.reload", "tp.reload")))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes reload <config|pipes> &7- &bReloads all pipes or the config."));
					if (cs.hasPermission(getConfig().getString("permissions.update", "tp.update")))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes update &7- &bChecks for a new plugin version at SpigotMC and updates the plugin if possible."));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m--------------------------------------------"));
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
		Bukkit.getPluginManager().registerEvents(new PipeNeighborBlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new HitboxListener(), this);
		Bukkit.getPluginManager().registerEvents(pipePacketManager, this);
		Bukkit.getPluginManager().registerEvents(updateManager = new UpdateManager(this), this);

		for (World world : Bukkit.getWorlds()) {
			SavingManager.loadPipesSync(world);
		}

		//Pipe
		PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = PIPE_ITEM.getItemMeta();
		meta.setDisplayName(PipeColor.WHITE.getColorCode() + PIPE_NAME);
		PIPE_ITEM.setItemMeta(meta);
		//Golden Pipe
		GOLDEN_PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		meta = GOLDEN_PIPE_ITEM.getItemMeta();
		meta.setDisplayName(GOLDEN_PIPE_NAME);
		GOLDEN_PIPE_ITEM.setItemMeta(meta);
		//Iron Pipe
		IRON_PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		meta = IRON_PIPE_ITEM.getItemMeta();
		meta.setDisplayName(IRON_PIPE_NAME);
		IRON_PIPE_ITEM.setItemMeta(meta);
		//Ice Pipe
		ICE_PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		meta = ICE_PIPE_ITEM.getItemMeta();
		meta.setDisplayName(ICE_PIPE_NAME);
		ICE_PIPE_ITEM.setItemMeta(meta);
		//Wrench
		WRENCH_ITEM = new ItemStack(Material.REDSTONE);
		meta = WRENCH_ITEM.getItemMeta();
		meta.setDisplayName(WRENCH_NAME);
		WRENCH_ITEM.setItemMeta(meta);

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

	public static void putPipe(Pipe pipe) {
		Map<BlockLoc, Pipe> pipeMap = getPipeMap(pipe.blockLoc.getWorld());
		if (pipeMap == null) {
			pipeMap = Collections.synchronizedMap(new TreeMap<BlockLoc, Pipe>());
			ppipes.put(pipe.blockLoc.getWorld(), pipeMap);
		}
		pipeMap.put(convertBlockLoc(pipe.blockLoc), pipe);
	}

	public static boolean canBuild(Player p, Block b, Block placedAgainst, EquipmentSlot es) {
		BlockBreakEvent bbe = new BlockBreakEvent(b, p);

		//unregister anticheat listeners
		List<RegisteredListener> unregisterListeners = new ArrayList<RegisteredListener>();
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

	public ItemStack getPipeItem(PipeColor pipeColor, boolean icePipe) {
		ItemStack result = PIPE_ITEM.clone();
		result.setAmount(1);
		ItemMeta itemMeta = result.getItemMeta();
		if (icePipe) {
			itemMeta.setDisplayName(ICE_PIPE_NAME);
		} else {
			itemMeta.setDisplayName(pipeColor.getColorCode() + PIPE_NAME);
		}
		result.setItemMeta(itemMeta);
		return result;
	}

	public ItemStack getGoldenPipeItem() {
		return GOLDEN_PIPE_ITEM;
	}

	public ItemStack getIronPipeItem() {
		return IRON_PIPE_ITEM;
	}

	public ItemStack getIcePipeItem() {
		return ICE_PIPE_ITEM;
	}

	public ItemStack getWrenchItem() {
		return WRENCH_ITEM;
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
