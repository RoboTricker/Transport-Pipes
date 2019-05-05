package de.robotricker.transportpipes.utils;

import com.comphenix.packetwrapper.WrapperPlayServerTitle;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.ThreadService;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.location.BlockLocation;

public class WorldUtils {

    private static Map<Player, Integer> hidingDuctsTimers = new HashMap<>();

    /**
     * THREAD-SAFE
     */
    public static List<Player> getPlayerList(World world) {
        // Bukkit.getOnlinePlayers is the only thread safe playerlist getter
        List<Player> playerList = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getWorld().equals(world)) {
                playerList.add(p);
            }
        }
        return playerList;
    }

    public static boolean isContainerBlock(Material material) {
        switch (material) {
            case DISPENSER:
            case CHEST:
            case FURNACE:
            case TRAPPED_CHEST:
            case DROPPER:
            case HOPPER:
            case BREWING_STAND:
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    public static boolean lwcProtection(Block b) {
        if (Bukkit.getPluginManager().isPluginEnabled("LWC")) {
            try {
                com.griefcraft.model.Protection protection = com.griefcraft.lwc.LWC.getInstance().findProtection(b);
                return protection != null && protection.getType() != com.griefcraft.model.Protection.Type.PUBLIC;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void startShowHiddenDuctsProcess(Player p, GlobalDuctManager globalDuctManager, ThreadService threadService, TransportPipes transportPipes, GeneralConf generalConf, PlayerSettingsService playerSettingsService) {

        int renderDistance = playerSettingsService.getOrCreateSettingsConf(p).getRenderDistance();

        if (hidingDuctsTimers.containsKey(p)) {
            return;
        }
        Set<Duct> showingDucts = new HashSet<>();
        Map<BlockLocation, Duct> ductMap = globalDuctManager.getDucts(p.getWorld());
        synchronized (globalDuctManager.getDucts()) {
            for (BlockLocation bl : ductMap.keySet()) {
                Duct duct = ductMap.get(bl);
                Block ductBlock = duct.getBlockLoc().toBlock(duct.getWorld());
                if (ductBlock.getLocation().distance(p.getLocation()) > renderDistance) {
                    continue;
                }
                if (duct.obfuscatedWith() != null && ductBlock.getBlockData().getMaterial() != Material.BARRIER) {
                    showingDucts.add(duct);
                    ductBlock.setBlockData(Material.BARRIER.createBlockData(), false);
                    threadService.tickDuctSpawnAndDespawn(duct);
                }
            }
        }

        int duration = generalConf.getShowHiddenDuctsTime();
        int[] task = new int[]{0};
        hidingDuctsTimers.put(p, duration);

        task[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(transportPipes, () -> {

            WrapperPlayServerTitle titlePacket = new WrapperPlayServerTitle();
            titlePacket.setTitle(WrappedChatComponent.fromText(LangConf.Key.SHOW_HIDDEN_DUCTS.get(hidingDuctsTimers.get(p))));
            titlePacket.setAction(EnumWrappers.TitleAction.ACTIONBAR);
            titlePacket.sendPacket(p);

            if (hidingDuctsTimers.get(p) == 0) {
                hidingDuctsTimers.remove(p);
                for (Duct duct : showingDucts) {
                    if (globalDuctManager.getDuctAtLoc(duct.getWorld(), duct.getBlockLoc()) != duct) {
                        continue;
                    }
                    Block ductBlock = duct.getBlockLoc().toBlock(duct.getWorld());
                    if (ductBlock.getBlockData().getMaterial() == Material.BARRIER) {
                        ductBlock.setBlockData(duct.obfuscatedWith() == null ? Material.AIR.createBlockData() : duct.obfuscatedWith(), false);
                        threadService.tickDuctSpawnAndDespawn(duct);
                    }
                }
                Bukkit.getScheduler().cancelTask(task[0]);
            } else {
                hidingDuctsTimers.put(p, hidingDuctsTimers.get(p) - 1);
            }
        }, 0L, 20L);

    }

}
