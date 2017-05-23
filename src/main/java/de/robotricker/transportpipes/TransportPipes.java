package de.robotricker.transportpipes;

import java.util.Collections;
import java.util.HashMap;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.transportpipes.manager.saving.SavingManager;
import de.robotricker.transportpipes.manager.settings.GoldenPipeInv;
import de.robotricker.transportpipes.manager.settings.SettingsInv;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.CraftUtils;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeNeighborBlockListener;
import de.robotricker.transportpipes.pipeutils.hitbox.HitboxListener;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.PipePacketManager;

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

	public String PREFIX_CONSOLE;

	public String PIPE_NAME;
	public static ItemStack PIPE_ITEM;
	public String GOLDEN_PIPE_NAME;
	public static ItemStack GOLDEN_PIPE_ITEM;
	public String IRON_PIPE_NAME;
	public static ItemStack IRON_PIPE_ITEM;
	public String WRENCH_NAME;
	public static ItemStack WRENCH_ITEM;

	//x << 34 | y << 26 | z
	public static Map<World, Map<BlockLoc, Pipe>> ppipes = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Pipe>>());

	public static ArmorStandProtocol armorStandProtocol;
	public static TransportPipes instance;
	public static PipeThread pipeThread;
	public static PipePacketManager pipePacketManager;

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
		saveConfig();

		PREFIX_CONSOLE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix"));
		PIPE_NAME = ChatColor.translateAlternateColorCodes('&', getConfig().getString("pipename.pipe"));
		GOLDEN_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&6" + getConfig().getString("pipename.golden_pipe"));
		IRON_PIPE_NAME = ChatColor.translateAlternateColorCodes('&', "&7" + getConfig().getString("pipename.iron_pipe"));
		WRENCH_NAME = ChatColor.translateAlternateColorCodes('&', "&c" + getConfig().getString("pipename.wrench"));

		PipeThread.setRunning(true);
		pipeThread = new PipeThread();
		pipeThread.setDaemon(true);
		pipeThread.setPriority(Thread.MIN_PRIORITY);
		pipeThread.start();

		SettingsInv settingsInv = new SettingsInv();

		getCommand("tt").setExecutor(new CommandExecutor() {
			
			@Override
			public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
				Player p = (Player) arg0;
				p.getInventory().addItem(new ItemStack(Integer.parseInt(arg3[0])));
				return true;
			}
		});
		getCommand("transportpipessettings").setExecutor(settingsInv);
		getCommand("transportpipestps").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

				int tps = PipeThread.getCalculatedTps();
				ChatColor colour = ChatColor.DARK_GREEN;
				if (tps <= 1) {
					colour = ChatColor.DARK_RED;
				} else if (tps <= 3) {
					colour = ChatColor.RED;
				} else if (tps <= 4) {
					colour = ChatColor.GOLD;
				} else if (tps <= 5) {
					colour = ChatColor.GREEN;
				}

				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-"));
				cs.sendMessage(ChatColor.GOLD + "TPS: " + colour + tps + " " + ChatColor.GOLD + "/ " + ChatColor.DARK_GREEN + PipeThread.WANTED_TPS);
				cs.sendMessage(ChatColor.GOLD + "Tick: " + colour + (PipeThread.timeTick / 10000) / 100f);
				for (World world : Bukkit.getWorlds()) {
					int worldPipes = 0;
					int worldItems = 0;
					Map<BlockLoc, Pipe> pipeMap = getPipeMap(world);
					if (pipeMap != null) {
						cs.sendMessage(ChatColor.YELLOW + world.getName() + ":");
						synchronized (pipeMap) {
							for (Pipe pipe : pipeMap.values()) {
								worldPipes++;
								worldItems += pipe.pipeItems.size() + pipe.tempPipeItems.size() + pipe.tempPipeItemsWithSpawn.size();
							}
						}
						cs.sendMessage(ChatColor.GOLD + "   Pipes: " + ChatColor.YELLOW + worldPipes);
						cs.sendMessage(ChatColor.GOLD + "   Items: " + ChatColor.YELLOW + worldItems);
					}
				}
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-"));
				return false;
			}
		});
		Bukkit.getPluginManager().registerEvents(settingsInv, this);
		Bukkit.getPluginManager().registerEvents(new CraftUtils(), this);
		Bukkit.getPluginManager().registerEvents(new GoldenPipeInv(), this);
		Bukkit.getPluginManager().registerEvents(new SavingManager(), this);
		Bukkit.getPluginManager().registerEvents(new PipeNeighborBlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new HitboxListener(), this);
		Bukkit.getPluginManager().registerEvents(pipePacketManager, this);

		for (World world : Bukkit.getWorlds()) {
			SavingManager.loadPipesSync(world);
		}

		PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = PIPE_ITEM.getItemMeta();
		meta.setDisplayName(PipeColor.WHITE.getColorCode() + PIPE_NAME);
		PIPE_ITEM.setItemMeta(meta);
		GOLDEN_PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		meta = GOLDEN_PIPE_ITEM.getItemMeta();
		meta.setDisplayName(GOLDEN_PIPE_NAME);
		GOLDEN_PIPE_ITEM.setItemMeta(meta);
		IRON_PIPE_ITEM = new ItemStack(Material.BLAZE_ROD);
		meta = IRON_PIPE_ITEM.getItemMeta();
		meta.setDisplayName(IRON_PIPE_NAME);
		IRON_PIPE_ITEM.setItemMeta(meta);
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

	public static boolean canBuild(Player p, Block b) {
		boolean canBuild = true;

		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			com.sk89q.worldguard.bukkit.WorldGuardPlugin wgp = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) TransportPipes.instance.getServer().getPluginManager().getPlugin("WorldGuard");
			canBuild = wgp.canBuild(p, b.getLocation());
		}
		if (Bukkit.getPluginManager().isPluginEnabled("ASkyBlock") && canBuild) {
			canBuild = com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance().locationIsOnIsland(p, b.getLocation());
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlotSquared") && canBuild) {
			com.intellectualcrafters.plot.api.PlotAPI plotApi = new com.intellectualcrafters.plot.api.PlotAPI();
			com.intellectualcrafters.plot.object.Plot plot = plotApi.getPlot(b.getLocation());
			if (plot != null) {
				canBuild = plotApi.getPlayerPlots(b.getWorld(), p).contains(plot);
			}
		}
		if (Bukkit.getPluginManager().isPluginEnabled("Factions") && canBuild) {
			com.massivecraft.factions.entity.MPlayer mp = com.massivecraft.factions.entity.MPlayer.get(p);
			com.massivecraft.factions.entity.Faction faction = com.massivecraft.factions.entity.BoardColl.get().getFactionAt(com.massivecraft.massivecore.ps.PS.valueOf(b));
			if (faction != null) {
				canBuild = faction.getMPlayers().contains(mp);
			}
		}
		if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention") && canBuild) {
			me.ryanhamshire.GriefPrevention.GriefPrevention gp = (me.ryanhamshire.GriefPrevention.GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
			String errorMsg = gp.allowBuild(p, b.getLocation());
			canBuild = errorMsg == null || errorMsg.isEmpty();
		}
		return canBuild || p.isOp();
	}

	public static BlockLoc convertBlockLoc(Location blockLoc) {
		return new BlockLoc(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
	}

	public static String getFormattedConfigString(String key) {
		return ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString(key));
	}

	public ItemStack getPipeItem(PipeColor pipeColor) {
		ItemStack result = PIPE_ITEM.clone();
		result.setAmount(1);
		ItemMeta itemMeta = result.getItemMeta();
		itemMeta.setDisplayName(pipeColor.getColorCode() + PIPE_NAME);
		result.setItemMeta(itemMeta);
		return result;
	}

	public ItemStack getGoldenPipeItem() {
		return GOLDEN_PIPE_ITEM;
	}

	public ItemStack getIronPipeItem() {
		return IRON_PIPE_ITEM;
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
