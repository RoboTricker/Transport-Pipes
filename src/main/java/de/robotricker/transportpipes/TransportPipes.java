package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.pipeutils.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeInv;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.CraftUtils;
import de.robotricker.transportpipes.pipeutils.PipeNeighborBlockUtils;
import de.robotricker.transportpipes.pipeutils.commands.CreativeCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadConfigCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadPipesCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.SettingsCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.TPSCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.UpdateCommandExecutor;
import de.robotricker.transportpipes.pipeutils.config.GeneralConf;
import de.robotricker.transportpipes.pipeutils.config.LocConf;
import de.robotricker.transportpipes.pipeutils.hitbox.HitboxListener;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.PipePacketManager;
import de.robotricker.transportpipes.rendersystem.PipeRenderSystem;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeRenderSystem;
import de.robotricker.transportpipes.rendersystem.vanilla.utils.VanillaPipeRenderSystem;
import de.robotricker.transportpipes.saving.SavingManager;
import de.robotricker.transportpipes.settings.SettingsInv;
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

	public static TransportPipes instance;
	
	private static PipeThread pipeThread;
	public static ArmorStandProtocol armorStandProtocol;
	public static PipePacketManager pipePacketManager;

	//x << 34 | y << 26 | z
	private Map<World, Map<BlockLoc, Pipe>> ppipes;

	private List<PipeRenderSystem> renderSystems;
	private UpdateManager updateManager;

	//configs
	public LocConf locConf;
	public GeneralConf generalConf;

	@Override
	public void onEnable() {
		instance = this;

		// Prepare collections
		ppipes = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Pipe>>());

		// Prepare managers
		armorStandProtocol = new ArmorStandProtocol();
		pipePacketManager = new PipePacketManager();

		locConf = new LocConf();
		generalConf = new GeneralConf();

		renderSystems = new ArrayList<>();
		renderSystems.add(new VanillaPipeRenderSystem(armorStandProtocol));
		renderSystems.add(new ModelledPipeRenderSystem(armorStandProtocol));

		PipeThread.setRunning(true);
		pipeThread = new PipeThread();
		pipeThread.setDaemon(true);
		pipeThread.setPriority(Thread.MIN_PRIORITY);
		pipeThread.start();

		//register command executors
		final SettingsCommandExecutor settingsCmdExec = new SettingsCommandExecutor();
		final TPSCommandExecutor tpsCmdExec = new TPSCommandExecutor();
		final CreativeCommandExecutor creativeCmdExec = new CreativeCommandExecutor();
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
					if (!settingsCmdExec.onCommand(cs)) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("creative")) {
					if (!creativeCmdExec.onCommand(cs)) {
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
					//TODO: header and footer in config
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m-----------&7&l[ &6TransportPipes " + TransportPipes.instance.getDescription().getVersion() + "&7&l]&7&l&m-----------"));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes settings &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_SETTINGS)));
					if (cs.hasPermission(generalConf.getPermissionTps()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes tps &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_TPS)));
					if (cs.hasPermission(generalConf.getPermissionCreative()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes creative &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_CREATIVE)));
					if (cs.hasPermission(generalConf.getPermissionReload()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes reload <config|pipes> &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_RELOAD)));
					if (cs.hasPermission(generalConf.getPermissionUpdate()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes update &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_UPDATE)));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m-------------------------------------------"));
					return true;
				}

				if (noPerm) {
					cs.sendMessage(LocConf.load(LocConf.COMMANDS_NOPERM));
				}

				return true;
			}
		});

		updateManager = new UpdateManager(this);
		
		//register listeners
		Bukkit.getPluginManager().registerEvents(new CraftUtils(), this);
		Bukkit.getPluginManager().registerEvents(new GoldenPipeInv(), this);
		Bukkit.getPluginManager().registerEvents(new SavingManager(), this);
		Bukkit.getPluginManager().registerEvents(new PipeNeighborBlockUtils(), this);
		Bukkit.getPluginManager().registerEvents(new HitboxListener(), this);
		Bukkit.getPluginManager().registerEvents(new SettingsInv(), this);
		Bukkit.getPluginManager().registerEvents(pipePacketManager, this);
		Bukkit.getPluginManager().registerEvents(updateManager, this);
		for (PipeRenderSystem prs : renderSystems) {
			Bukkit.getPluginManager().registerEvents(prs, this);
		}

		for (World world : Bukkit.getWorlds()) {
			SavingManager.loadPipesSync(world);
		}

		CraftUtils.initRecipes();

	}

	public UpdateManager getUpdateManager(){
		return updateManager;
	}
	
	public List<PipeRenderSystem> getPipeRenderSystems() {
		return renderSystems;
	}

	public String getFormattedWrenchName() {
		return "§c" + LocConf.load(LocConf.PIPES_WRENCH);
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

	public Map<BlockLoc, Pipe> getPipeMap(World world) {
		if (ppipes.containsKey(world)) {
			return ppipes.get(world);
		}
		return null;
	}
	
	public Map<World, Map<BlockLoc, Pipe>> getFullPipeMap(){
		return ppipes;
	}

}
