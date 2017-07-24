package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeInv;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.ContainerBlockUtils;
import de.robotricker.transportpipes.pipeutils.CraftUtils;
import de.robotricker.transportpipes.pipeutils.commands.CreativeCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.DeletePipesCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadConfigCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.ReloadPipesCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.SaveCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.SettingsCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.TPSCommandExecutor;
import de.robotricker.transportpipes.pipeutils.commands.UpdateCommandExecutor;
import de.robotricker.transportpipes.pipeutils.config.GeneralConf;
import de.robotricker.transportpipes.pipeutils.config.LocConf;
import de.robotricker.transportpipes.pipeutils.config.RecipesConf;
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

	public static PipeThread pipeThread;
	public static ArmorStandProtocol armorStandProtocol;
	public static PipePacketManager pipePacketManager;

	//x << 34 | y << 26 | z
	private Map<World, Map<BlockLoc, Pipe>> registeredPipes;
	private Map<World, Map<BlockLoc, TransportPipesContainer>> registeredContainers;

	private List<PipeRenderSystem> renderSystems;
	private UpdateManager updateManager;

	//configs
	public LocConf locConf;
	public GeneralConf generalConf;
	public RecipesConf recipesConf;

	@Override
	public void onEnable() {
		instance = this;
		
		// Prepare collections
		registeredPipes = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Pipe>>());
		registeredContainers = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, TransportPipesContainer>>());

		// Prepare managers
		armorStandProtocol = new ArmorStandProtocol();
		pipePacketManager = new PipePacketManager();

		locConf = new LocConf();
		generalConf = new GeneralConf();
		recipesConf = new RecipesConf();

		renderSystems = new ArrayList<>();
		renderSystems.add(new VanillaPipeRenderSystem(armorStandProtocol));
		renderSystems.add(new ModelledPipeRenderSystem(armorStandProtocol));

		pipeThread = new PipeThread();
		pipeThread.setDaemon(true);
		pipeThread.setPriority(Thread.MIN_PRIORITY);

		//register command executors
		final SettingsCommandExecutor settingsCmdExec = new SettingsCommandExecutor();
		final TPSCommandExecutor tpsCmdExec = new TPSCommandExecutor();
		final CreativeCommandExecutor creativeCmdExec = new CreativeCommandExecutor();
		final ReloadConfigCommandExecutor reloadConfigCmdExec = new ReloadConfigCommandExecutor();
		final ReloadPipesCommandExecutor reloadPipesCmdExec = new ReloadPipesCommandExecutor();
		final UpdateCommandExecutor updateCmdExec = new UpdateCommandExecutor();
		final SaveCommandExecutor saveCmdExec = new SaveCommandExecutor();
		final DeletePipesCommandExecutor deletePipesCmdExec = new DeletePipesCommandExecutor();

		getCommand("transportpipes").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

				boolean noPerm = false;

				if (args.length >= 1 && args[0].equalsIgnoreCase("tps")) {
					if (!tpsCmdExec.onCommand(cs, Arrays.copyOfRange(args, 1, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("settings")) {
					if (!settingsCmdExec.onCommand(cs, Arrays.copyOfRange(args, 1, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("creative")) {
					if (!creativeCmdExec.onCommand(cs, Arrays.copyOfRange(args, 1, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("update")) {
					if (!updateCmdExec.onCommand(cs, Arrays.copyOfRange(args, 1, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 2 && args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("config")) {
					if (!reloadConfigCmdExec.onCommand(cs, Arrays.copyOfRange(args, 2, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 2 && args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("pipes")) {
					if (!reloadPipesCmdExec.onCommand(cs, Arrays.copyOfRange(args, 2, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 1 && args[0].equalsIgnoreCase("save")) {
					if (!saveCmdExec.onCommand(cs, Arrays.copyOfRange(args, 1, args.length))) {
						noPerm = true;
					}
				} else if (args.length >= 2 && args[0].equalsIgnoreCase("delete")) {
					if (!deletePipesCmdExec.onCommand(cs, Arrays.copyOfRange(args, 1, args.length))) {
						noPerm = true;
					}
				} else {
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(LocConf.load(LocConf.COMMANDS_HEADER), TransportPipes.instance.getDescription().getVersion())));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes settings &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_SETTINGS)));
					if (cs.hasPermission(generalConf.getPermissionTps()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes tps &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_TPS)));
					if (cs.hasPermission(generalConf.getPermissionCreative()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes creative &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_CREATIVE)));
					if (cs.hasPermission(generalConf.getPermissionReload()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes reload <config|pipes> &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_RELOAD)));
					if (cs.hasPermission(generalConf.getPermissionUpdate()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes update &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_UPDATE)));
					if (cs.hasPermission(generalConf.getPermissionSave()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes save &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_SAVE)));
					if (cs.hasPermission(generalConf.getPermissionDelete()))
						cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/tpipes delete <Radius> &7- " + LocConf.load(LocConf.COMMANDS_DESCRIPTION_DELETE)));
					cs.sendMessage(ChatColor.translateAlternateColorCodes('&', LocConf.load(LocConf.COMMANDS_FOOTER)));
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
		Bukkit.getPluginManager().registerEvents(new ContainerBlockUtils(), this);
		Bukkit.getPluginManager().registerEvents(new HitboxListener(), this);
		Bukkit.getPluginManager().registerEvents(new SettingsInv(), this);
		Bukkit.getPluginManager().registerEvents(pipePacketManager, this);
		Bukkit.getPluginManager().registerEvents(updateManager, this);
		for (PipeRenderSystem prs : renderSystems) {
			Bukkit.getPluginManager().registerEvents(prs, this);
			if (prs instanceof ModelledPipeRenderSystem && Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
				Bukkit.getPluginManager().registerEvents(((ModelledPipeRenderSystem) prs).new AuthMeLoginListener(), this);
			}
		}

		for (World world : Bukkit.getWorlds()) {
			SavingManager.loadPipesSync(world);
		}

		CraftUtils.initRecipes();

		Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

			@Override
			public void run() {
				PipeThread.setRunning(true);
				pipeThread.start();
			}
		});

	}

	public UpdateManager getUpdateManager() {
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
		if (registeredPipes.containsKey(world)) {
			return registeredPipes.get(world);
		}
		return null;
	}

	public Map<World, Map<BlockLoc, Pipe>> getFullPipeMap() {
		return registeredPipes;
	}

	public Map<BlockLoc, TransportPipesContainer> getContainerMap(World world) {
		if (registeredContainers.containsKey(world)) {
			return registeredContainers.get(world);
		}
		return null;
	}

	public Map<World, Map<BlockLoc, TransportPipesContainer>> getFullContainerMap() {
		return registeredContainers;
	}

}
