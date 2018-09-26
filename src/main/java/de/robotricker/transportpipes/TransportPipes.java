package de.robotricker.transportpipes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

import de.robotricker.transportpipes.commands.TPCommandExecutor;
import de.robotricker.transportpipes.commands.TPSCommandExecutor;
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
        Sentry.init("https://84937d8c6bc2435d860021667341c87c@sentry.io/1281889?stacktrace.app.packages=de.robotricker&release=" + instance.getDescription().getVersion());
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

        TPCommandExecutor tpsCmd = new TPSCommandExecutor();

        getCommand("transportpipes").setExecutor((sender, command, label, args) -> {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("tps")) {
                    if(tpsCmd.onTPCommand(sender, Arrays.copyOfRange(args, 1, args.length))) {
                        return true;
                    }
                }
            }
            sender.sendMessage(wrapColoredMsg("&6/tpipes tps &7- Overview of the basic TransportPipes system"));
            return true;
        });

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

    public static String wrapColoredMsg(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
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
