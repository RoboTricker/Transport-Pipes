package de.robotricker.transportpipes;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import co.aikar.commands.PaperCommandManager;
import de.robotricker.transportpipes.api.TransportPipesAPI;
import de.robotricker.transportpipes.commands.TPCommand;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.factory.PipeFactory;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.manager.PipeManager;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.inventory.PlayerSettingsInventory;
import de.robotricker.transportpipes.items.PipeItemManager;
import de.robotricker.transportpipes.listener.DuctListener;
import de.robotricker.transportpipes.listener.PlayerListener;
import de.robotricker.transportpipes.listener.TPContainerListener;
import de.robotricker.transportpipes.listener.WorldListener;
import de.robotricker.transportpipes.log.LoggerService;
import de.robotricker.transportpipes.log.SentryService;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.ModelledPipeRenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.VanillaPipeRenderSystem;
import de.robotricker.transportpipes.saving.DiskService;
import de.robotricker.transportpipes.utils.LWCUtils;
import de.robotricker.transportpipes.utils.legacy.LegacyUtils;
import de.robotricker.transportpipes.utils.legacy.LegacyUtils_1_13;
import io.sentry.event.Breadcrumb;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TransportPipes extends JavaPlugin {

    private Injector injector;

    private SentryService sentry;
    private ThreadService thread;
    private DiskService diskService;

    @Override
    public void onEnable() {

        if (Bukkit.getVersion().contains("1.13")) {
            LegacyUtils.setInstance(new LegacyUtils_1_13());
        } else {
            System.err.println("------------------------------------------");
            System.err.println("TransportPipes currently only works with Minecraft 1.13.1 and 1.13.2");
            System.err.println("------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Class.forName("org.bukkit.inventory.RecipeChoice");
        } catch (ClassNotFoundException e) {
            System.err.println("------------------------------------------");
            System.err.println("TransportPipes currently only works with Minecraft 1.13.1 and 1.13.2");
            System.err.println("------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Files.isRegularFile(Paths.get(getDataFolder().getPath(), "recipes.yml"))) {
            System.err.println("------------------------------------------");
            System.err.println("Please delete the old plugins/TransportPipes directory so TransportPipes can recreate it with a bunch of new config values");
            System.err.println("------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        //Initialize dependency injector
        injector = new InjectorBuilder().addDefaultHandlers("de.robotricker.transportpipes").create();
        injector.register(Logger.class, getLogger());
        injector.register(Plugin.class, this);
        injector.register(JavaPlugin.class, this);
        injector.register(TransportPipes.class, this);

        //Initialize logger
        LoggerService logger = injector.getSingleton(LoggerService.class);

        //Initialize sentry
        sentry = injector.getSingleton(SentryService.class);
        if (!sentry.init("https://84937d8c6bc2435d860021667341c87c@sentry.io/1281889?stacktrace.app.packages=de.robotricker&release=" + getDescription().getVersion())) {
            logger.warning("Unable to initialize sentry!");
        }
        sentry.addTag("thread", Thread.currentThread().getName());
        sentry.injectThread(Thread.currentThread());
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "enabling plugin");

        //Initialize configs
        injector.getSingleton(GeneralConf.class);
        injector.register(LangConf.class, new LangConf(this, injector.getSingleton(GeneralConf.class).getLanguage()));

        //Initialize API
        injector.getSingleton(TransportPipesAPI.class);

        //Initialize thread
        thread = injector.getSingleton(ThreadService.class);
        thread.start();

        //Register pipe
        BaseDuctType<Pipe> baseDuctType = injector.getSingleton(DuctRegister.class).registerBaseDuctType("Pipe", PipeManager.class, PipeFactory.class, PipeItemManager.class);
        baseDuctType.setModelledRenderSystem(injector.newInstance(ModelledPipeRenderSystem.class));
        baseDuctType.setVanillaRenderSystem(injector.newInstance(VanillaPipeRenderSystem.class));

        //Register listeners
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(TPContainerListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(PlayerListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(DuctListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(WorldListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(PlayerSettingsInventory.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(ResourcepackService.class), this);

        //Register commands
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(injector.getSingleton(TPCommand.class));
        commandManager.getCommandCompletions().registerCompletion("baseDuctType", c -> injector.getSingleton(DuctRegister.class).baseDuctTypes().stream().map(BaseDuctType::getName).collect(Collectors.toList()));

        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "enabled plugin");

        diskService = injector.getSingleton(DiskService.class);

        TPContainerListener tpContainerListener = injector.getSingleton(TPContainerListener.class);
        runTaskSync(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk loadedChunk : world.getLoadedChunks()) {
                    tpContainerListener.handleChunkLoadSync(loadedChunk, true);
                }
                diskService.loadDuctsSync(world);
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("LWC")) {
            try {
                com.griefcraft.scripting.Module module = injector.getSingleton(LWCUtils.class);
                com.griefcraft.lwc.LWC.getInstance().getModuleLoader().registerModule(this, module);
            } catch (Exception e) {
                e.printStackTrace();
                sentry.record(e);
            }
        }

    }

    @Override
    public void onDisable() {
        if (sentry != null && thread != null) {
            sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "disabling plugin");
            // Stop tpThread gracefully
            try {
                thread.stopRunning();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (World world : Bukkit.getWorlds()) {
                saveWorld(world);
            }
            sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "disabled plugin");
        }
    }

    public void saveWorld(World world) {
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "saving world " + world.getName());
        diskService.saveDuctsSync(world);
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "saved world " + world.getName());
    }

    public void runTaskSync(Runnable task) {
        if (isEnabled()) {
            Bukkit.getScheduler().runTask(this, task);
        }
    }

    public void runTaskSyncLater(Runnable task, long delay) {
        if (isEnabled()) {
            Bukkit.getScheduler().runTaskLater(this, task, delay);
        }
    }

    public void runTaskAsync(Runnable runnable, long delay) {
        thread.getTasks().put(runnable, delay);
    }

    public Injector getInjector() {
        return injector;
    }

    public void changeRenderSystem(Player p, String newRenderSystemName) {
        PlayerSettingsConf playerSettingsConf = injector.getSingleton(PlayerSettingsService.class).getOrCreateSettingsConf(p);
        DuctRegister ductRegister = injector.getSingleton(DuctRegister.class);
        GlobalDuctManager globalDuctManager = injector.getSingleton(GlobalDuctManager.class);
        ProtocolService protocolService = injector.getSingleton(ProtocolService.class);

        // change render system
        String oldRenderSystemName = playerSettingsConf.getRenderSystemName();
        if (oldRenderSystemName.equalsIgnoreCase(newRenderSystemName)) {
            return;
        }
        playerSettingsConf.setRenderSystemName(newRenderSystemName);

        for (BaseDuctType baseDuctType : ductRegister.baseDuctTypes()) {
            RenderSystem oldRenderSystem = RenderSystem.getRenderSystem(oldRenderSystemName, baseDuctType);

            // switch render system
            synchronized (globalDuctManager.getPlayerDucts(p)) {
                Iterator<Duct> ductIt = globalDuctManager.getPlayerDucts(p).iterator();
                while (ductIt.hasNext()) {
                    Duct nextDuct = ductIt.next();
                    protocolService.removeASD(p, oldRenderSystem.getASDForDuct(nextDuct));
                    ductIt.remove();
                }
            }

        }
    }

    public long convertVersionToLong(String version) {
        long versionLong = 0;
        try {
            if (version.contains("-")) {
                for (String subVersion : version.split("-")) {
                    if (subVersion.startsWith("b")) {
                        int buildNumber = 0;
                        String buildNumberString = subVersion.substring(1);
                        if (!buildNumberString.equalsIgnoreCase("CUSTOM")) {
                            buildNumber = Integer.parseInt(buildNumberString);
                        }
                        versionLong |= buildNumber;
                    } else if (!subVersion.equalsIgnoreCase("SNAPSHOT")) {
                        versionLong |= (long) convertMainVersionStringToInt(subVersion) << 32;
                    }
                }
            } else {
                versionLong = (long) convertMainVersionStringToInt(version) << 32;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionLong;
    }

    private int convertMainVersionStringToInt(String mainVersion) {
        int versionInt = 0;
        if (mainVersion.contains(".")) {
            // shift for every version number 1 byte to the left
            int leftShift = (mainVersion.split("\\.").length - 1) * 8;
            for (String subVersion : mainVersion.split("\\.")) {
                byte v = Byte.parseByte(subVersion);
                versionInt |= ((int) v << leftShift);
                leftShift -= 8;
            }
        } else {
            versionInt = Byte.parseByte(mainVersion);
        }
        return versionInt;
    }

}
