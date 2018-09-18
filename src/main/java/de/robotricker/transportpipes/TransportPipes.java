package de.robotricker.transportpipes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import io.sentry.Sentry;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;

public class TransportPipes extends JavaPlugin {

    public static TransportPipes instance;

    private TPThread tpThread;
    private DuctManager ductManager;

    @Override
    public void onEnable() {
        instance = this;

        Sentry.init("https://84937d8c6bc2435d860021667341c87c@sentry.io/1281889?stacktrace=de.robotricker&release=" + instance.getDescription().getVersion());
        Sentry.getContext().addTag("thread", Thread.currentThread().getName());
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            Sentry.capture(e);
        });
        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setLevel(Breadcrumb.Level.INFO).setCategory("MAIN").setMessage("enabling plugin").build());

        ductManager = new DuctManager();
        ductManager.register();
        Bukkit.getPluginManager().registerEvents(ductManager.new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DuctListener(), this);

        tpThread = new TPThread();
        tpThread.start();

        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setLevel(Breadcrumb.Level.INFO).setCategory("MAIN").setMessage("enabled plugin").build());
    }

    @Override
    public void onDisable() {
        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setLevel(Breadcrumb.Level.INFO).setCategory("MAIN").setMessage("disabling plugin").build());
        try {
            tpThread.stopRunning();
            tpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setLevel(Breadcrumb.Level.INFO).setCategory("MAIN").setMessage("disabled plugin").build());
    }

    public DuctManager getDuctManager() {
        return ductManager;
    }

    public TPThread getTPThread() {
        return tpThread;
    }

    // *****************************************
    // STATIC UTILS
    // *****************************************

    public static void logDebug(String log) {
        instance.getLogger().fine(log);
    }

    public static void logInfo(String log) {
        instance.getLogger().info(log);
    }

    public static void logWarn(String log) {
        instance.getLogger().warning(log);
    }

    public static void logError(String log) {
        instance.getLogger().severe(log);
    }

    public static void runTask(Runnable task) {
        if (instance.isEnabled()) {
            Bukkit.getScheduler().runTask(instance, task);
        }
    }

    public static void runTaskLater(Runnable task, long delay) {
        if (instance.isEnabled()) {
            Bukkit.getScheduler().runTaskLater(instance, task, delay);
        }
    }

}
