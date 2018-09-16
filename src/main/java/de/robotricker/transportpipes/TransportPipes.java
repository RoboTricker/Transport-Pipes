package de.robotricker.transportpipes;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import io.sentry.Sentry;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;

public class TransportPipes extends JavaPlugin {

    public static TransportPipes instance;
    private TPThread tpThread;
    private Map<World, Map<BlockLoc, Duct>> ducts;

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

        ducts = Collections.synchronizedMap(new HashMap<>());
        DuctType.registerDuctType(new DuctType("Pipe"));

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

    public Map<World, Map<BlockLoc, Duct>> getDucts() {
        return ducts;
    }

    public Map<BlockLoc, Duct> getDucts(World world) {
        if (ducts.containsKey(world)) {
            return ducts.get(world);
        }
        ducts.put(world, new TreeMap<>());
        return ducts.get(world);
    }

    public TPThread getTpThread() {
        return tpThread;
    }

    //static utils functions

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
