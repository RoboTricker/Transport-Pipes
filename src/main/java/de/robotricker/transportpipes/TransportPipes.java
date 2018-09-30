package de.robotricker.transportpipes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import javax.inject.Provider;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import co.aikar.commands.PaperCommandManager;
import de.robotricker.transportpipes.commands.TPCommand;
import de.robotricker.transportpipes.listener.DuctListener;
import de.robotricker.transportpipes.listener.PlayerListener;
import de.robotricker.transportpipes.log.LoggerService;
import de.robotricker.transportpipes.log.SentryService;
import de.robotricker.transportpipes.protocol.ProtocolService;
import io.sentry.event.Breadcrumb;

public class TransportPipes extends JavaPlugin {

    private Injector injector;

    private SentryService sentry;
    private TPThread thread;

    @Override
    public void onEnable() {

        //Initialize dependency injector
        injector = new InjectorBuilder().addDefaultHandlers("de.robotricker.transportpipes").create();
        injector.register(Logger.class, getLogger());
        injector.register(JavaPlugin.class, this);

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

        //Initialize thread
        thread = injector.getSingleton(TPThread.class);
        thread.start();

        //Register duct service
        injector.getSingleton(DuctService.class).register();

        //Register listeners
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(PlayerListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getSingleton(DuctListener.class), this);

        //Register commands
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(injector.getSingleton(TPCommand.class));

        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "enabled plugin");
    }

    @Override
    public void onDisable() {
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "disabling plugin");
        // Stop tpThread gracefully
        try {
            thread.stopRunning();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "disabled plugin");
    }

}
