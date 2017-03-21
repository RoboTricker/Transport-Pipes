package de.robotricker.transportpipes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import de.robotricker.transportpipes.manager.saving.SavingManager;
import de.robotricker.transportpipes.manager.settings.GoldenPipeInv;
import de.robotricker.transportpipes.manager.settings.SettingsInv;
import de.robotricker.transportpipes.pipes.Pipe;
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

	public static final String PREFIX_CONSOLE = "[TransportPipes] ";

	public static final String PIPE_NAME = ChatColor.WHITE + "Pipe";
	public static ItemStack PIPE_ITEM;
	public static final String GOLDEN_PIPE_NAME = ChatColor.GOLD + "Golden-Pipe";
	public static ItemStack GOLDEN_PIPE_ITEM;
	public static final String IRON_PIPE_NAME = ChatColor.GRAY + "Iron-Pipe";
	public static ItemStack IRON_PIPE_ITEM;
	public static final String WRENCH_NAME = ChatColor.RED + "Wrench";
	public static ItemStack WRENCH_ITEM;

	//x << 34 | y << 26 | z
	public static Map<World, Map<Long, Pipe>> ppipes = Collections.synchronizedMap(new HashMap<World, Map<Long, Pipe>>());

	public static ArmorStandProtocol armorStandProtocol;
	public static TransportPipes instance;
	public static PipeThread pipeThread;
	public static PipePacketManager pipePacketManager;

	@Override
	public void onEnable() {
		instance = this;
		armorStandProtocol = new ArmorStandProtocol(ProtocolLibrary.getProtocolManager());
		pipePacketManager = new PipePacketManager();

		PipeThread.setRunning(true);
		pipeThread = new PipeThread();
		pipeThread.setDaemon(true);
		pipeThread.setPriority(Thread.MIN_PRIORITY);
		pipeThread.start();

		SettingsInv settingsInv = new SettingsInv();

		getCommand("transportpipessettings").setExecutor(settingsInv);
		getCommand("transportpipestps").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

				int tps = PipeThread.getCalculatedTps();
				ChatColor colour = ChatColor.DARK_GREEN;
				if (tps <= 1) {
					colour = ChatColor.DARK_RED;
				} else if (tps <= 4) {
					colour = ChatColor.RED;
				} else if (tps <= 5) {
					colour = ChatColor.GOLD;
				} else if (tps <= 6) {
					colour = ChatColor.GREEN;
				}

				cs.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-"));
				cs.sendMessage(ChatColor.GOLD + "TPS: " + colour + tps + " " + ChatColor.GOLD +"/ "
						+ ChatColor.DARK_GREEN + PipeThread.WANTED_TPS);
				cs.sendMessage(ChatColor.GOLD + "Tick: " + colour + (PipeThread.timeTick / 10000) / 100f);
				for (World world : Bukkit.getWorlds()) {
					int worldPipes = 0;
					int worldItems = 0;
					Map<Long, Pipe> pipeMap = getPipeMap(world);
					if (pipeMap != null) {
						cs.sendMessage(ChatColor.YELLOW + world.getName() + ":");
						synchronized (pipeMap) {
							for (Pipe pipe : pipeMap.values()) {
								worldPipes++;
								worldItems += pipe.pipeItems.size();
							}
						}
						cs.sendMessage(ChatColor.GOLD + "   Pipes: " + ChatColor.YELLOW + worldPipes);
						cs.sendMessage(ChatColor.GOLD + "   Items: " + ChatColor.YELLOW + worldItems);
					}
				}
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-&6-&e-"));
				return false;
			}
		});
		Bukkit.getPluginManager().registerEvents(settingsInv, this);
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
		meta.setDisplayName(PIPE_NAME);
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

		//Recipes
		ItemStack result = PIPE_ITEM.clone();
		result.setAmount(2);
		ShapedRecipe recipe = new ShapedRecipe(result);
		recipe.shape("AAA", "BBB", "AAA");
		recipe.setIngredient('A', new MaterialData(Material.STICK, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(recipe);

		result = GOLDEN_PIPE_ITEM.clone();
		result.setAmount(1);
		recipe = new ShapedRecipe(result);
		recipe.shape("XAX", "ABA", "XAX");
		recipe.setIngredient('A', new MaterialData(Material.GOLD_INGOT, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(recipe);

		result = IRON_PIPE_ITEM.clone();
		result.setAmount(2);
		recipe = new ShapedRecipe(result);
		recipe.shape("XAX", "ABA", "XAX");
		recipe.setIngredient('A', new MaterialData(Material.IRON_INGOT, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.GLASS, (byte) 0));
		Bukkit.addRecipe(recipe);

		result = WRENCH_ITEM.clone();
		result.setAmount(1);
		recipe = new ShapedRecipe(result);
		recipe.shape("XAX", "ABA", "XAX");
		recipe.setIngredient('A', new MaterialData(Material.REDSTONE, (byte) 0));
		recipe.setIngredient('B', new MaterialData(Material.STICK, (byte) 0));
		Bukkit.addRecipe(recipe);

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

	public static Map<Long, Pipe> getPipeMap(World world) {
		if (ppipes.containsKey(world)) {
			return ppipes.get(world);
		}
		return null;
	}

	public static void putPipe(Pipe pipe) {
		Map<Long, Pipe> pipeMap = getPipeMap(pipe.blockLoc.getWorld());
		if (pipeMap == null) {
			pipeMap = Collections.synchronizedMap(new TreeMap<Long, Pipe>());
			ppipes.put(pipe.blockLoc.getWorld(), pipeMap);
		}
		pipeMap.put(blockLocToLong(pipe.blockLoc), pipe);
	}

	public static boolean canBuild(Player p, Block b) {
		boolean canBuild = true;
		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			canBuild = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().canBuild(p, b);
		}
		if (Bukkit.getPluginManager().isPluginEnabled("ASkyBlock") && canBuild) {
			canBuild = com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance().locationIsOnIsland(p, b.getLocation());
		}
		return canBuild || p.isOp();
	}

	public static long blockLocToLong(Location blockLoc) {
		return (((long) (blockLoc.getBlockX() + 30000)) << 34) | ((long) (blockLoc.getBlockY()) << 26) | ((long) (blockLoc.getBlockZ() + 30000));
	}

}
