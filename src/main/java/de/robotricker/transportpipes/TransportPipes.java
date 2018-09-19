package de.robotricker.transportpipes;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import de.robotricker.transportpipes.listener.DuctListener;
import de.robotricker.transportpipes.log.LoggerService;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.log.SentryService;
import io.sentry.event.Breadcrumb;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class TransportPipes extends JavaPlugin {

    private final static String SENTRY_DSN = "https://84937d8c6bc2435d860021667341c87c@sentry.io/1281889?stacktrace=de.robotricker&release=";

    private Injector injector;

    // Services
    private LoggerService logger;
    private SentryService sentry;
    private PipeThread thread;
    private ProtocolService protocol;
    private DuctManager ductManager;

    @Override
    public void onEnable() {
        // Initialize the dependency injector
        injector = new InjectorBuilder().addDefaultHandlers("de.robotricker.transportpipes").create();
        injector.register(Logger.class, getLogger());

        // Initialize logger
        logger = injector.getSingleton(LoggerService.class);

        // Initialize sentry
        sentry = injector.getSingleton(SentryService.class);
        if (!sentry.initialize(SENTRY_DSN + getDescription().getVersion())) {
            logger.warning("Unable to initialize sentry!");
        }
        sentry.addTag("thread", Thread.currentThread().getName());
        sentry.injectThread(Thread.currentThread());
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "enabling plugin");

        // Initialize thread
        thread = injector.getSingleton(PipeThread.class);
        thread.start();

        // Initialize protocol service
        protocol = injector.getSingleton(ProtocolService.class);

        // Initialize duct manager
        ductManager = injector.getSingleton(DuctManager.class);
        ductManager.register();

        // Register listeners
        getServer().getPluginManager().registerEvents(injector.getSingleton(DuctListener.class), this);
        getServer().getPluginManager().registerEvents(injector.getSingleton(DuctListener.class), this);

        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "plugin enabled");
    }

    @Override
    public void onDisable() {
        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "disabling plugin");

        // Stop pipe thread gracefully
        try {
            thread.stopRunning();
            thread.join();
        } catch (InterruptedException e) {
            logger.error("Unable to stop the pipe thread gracefully!", e);
        }

        sentry.breadcrumb(Breadcrumb.Level.INFO, "MAIN", "plugin disabled");
    }
}
