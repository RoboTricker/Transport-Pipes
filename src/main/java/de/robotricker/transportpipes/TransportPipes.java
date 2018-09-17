package de.robotricker.transportpipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctType;
import de.robotricker.transportpipes.ducts.pipe.ColoredPipeType;
import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.ModelledPipeRenderSystem;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.VanillaPipeRenderSystem;
import de.robotricker.transportpipes.utils.BlockLoc;
import io.sentry.Sentry;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;

public class TransportPipes extends JavaPlugin {

    public static TransportPipes instance;

    private TPThread tpThread;
    private Map<World, Map<BlockLoc, Duct>> ducts;
    private Map<DuctType, List<RenderSystem>> renderSystems;

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

        renderSystems = new HashMap<>();
        ducts = Collections.synchronizedMap(new HashMap<>());
        //duct registration
        DuctType.registerDuctType(new DuctType("Pipe"));
        //pipe registration
        PipeType.registerPipeType(new ColoredPipeType("White", "§7"));
        PipeType.registerPipeType(new ColoredPipeType("Blue", "§1"));
        PipeType.registerPipeType(new ColoredPipeType("Red", "§4"));
        PipeType.registerPipeType(new ColoredPipeType("Yellow", "§e"));
        PipeType.registerPipeType(new ColoredPipeType("Green", "§2"));
        PipeType.registerPipeType(new ColoredPipeType("Black", "§8"));
        PipeType.registerPipeType(new PipeType("Golden", "§6"));
        PipeType.registerPipeType(new PipeType("Iron", "§7"));
        PipeType.registerPipeType(new PipeType("Ice", "§b"));
        PipeType.registerPipeType(new PipeType("Void", "§5"));
        PipeType.registerPipeType(new PipeType("Extraction", "§d"));
        PipeType.registerPipeType(new PipeType("Crafting", "§e"));
        //render system registration
        registerRenderSystem(DuctType.valueOf("Pipe"), new VanillaPipeRenderSystem());
        registerRenderSystem(DuctType.valueOf("Pipe"), new ModelledPipeRenderSystem());

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

    public Map<DuctType, List<RenderSystem>> getRenderSystems() {
        return renderSystems;
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

    public TPThread getTPThread() {
        return tpThread;
    }

    public void registerRenderSystem(DuctType ductType, RenderSystem renderSystem) {
        renderSystems.computeIfAbsent(ductType, k -> new ArrayList<>()).add(renderSystem);
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
