package de.robotricker.transportpipes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.log.SentryService;
import de.robotricker.transportpipes.rendersystems.ModelledRenderSystem;
import de.robotricker.transportpipes.rendersystems.VanillaRenderSystem;

public class ResourcepackService implements Listener {

    private static String URL = "https://raw.githubusercontent.com/RoboTricker/Transport-Pipes/master/src/main/resources/wiki/resourcepack.zip";

    private SentryService sentry;
    private TransportPipes transportPipes;
    private PlayerSettingsService playerSettingsService;

    private ResourcepackMode resourcepackMode;
    private byte[] cachedHash;
    private Set<Player> resourcepackPlayers;
    private Set<Player> loadingResourcepackPlayers;
    private Set<Player> waitingForAuthmeLogin;

    @Inject
    public ResourcepackService(SentryService sentry, TransportPipes transportPipes, PlayerSettingsService playerSettingsService, GeneralConf generalConf) {
        this.sentry = sentry;
        this.transportPipes = transportPipes;
        this.playerSettingsService = playerSettingsService;
        this.resourcepackMode = generalConf.getResourcepackMode();
        if (resourcepackMode == ResourcepackMode.DEFAULT) {
            cachedHash = calcSHA1Hash(URL);
        }
        resourcepackPlayers = new HashSet<>();
        loadingResourcepackPlayers = new HashSet<>();
        waitingForAuthmeLogin = new HashSet<>();

        if (Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onAuthMeLogin(fr.xephi.authme.events.LoginEvent e) {
                    if (waitingForAuthmeLogin.remove(e.getPlayer())) {
                        loadResourcepackForPlayer(e.getPlayer());
                    }
                }
            }, transportPipes);
        }
    }

    public byte[] getCachedHash() {
        return cachedHash;
    }

    public Set<Player> getResourcepackPlayers() {
        return resourcepackPlayers;
    }

    public ResourcepackMode getResourcepackMode() {
        return resourcepackMode;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (getResourcepackMode() == ResourcepackMode.NONE) {
            transportPipes.changeRenderSystem(e.getPlayer(), VanillaRenderSystem.getDisplayName());
        } else if (getResourcepackMode() == ResourcepackMode.DEFAULT) {
            PlayerSettingsConf conf = playerSettingsService.getOrCreateSettingsConf(e.getPlayer());
            if (conf.getRenderSystemName().equalsIgnoreCase(ModelledRenderSystem.getDisplayName())) {

                transportPipes.changeRenderSystem(e.getPlayer(), VanillaRenderSystem.getDisplayName());

                if (!Bukkit.getPluginManager().isPluginEnabled("AuthMe") || fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(e.getPlayer())) {
                    transportPipes.runTaskSync(() -> loadResourcepackForPlayer(e.getPlayer()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("AuthMe") && !fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(e.getPlayer())) {
                    waitingForAuthmeLogin.add(e.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onResourcepackStatus(PlayerResourcePackStatusEvent e) {
        if (getResourcepackMode() != ResourcepackMode.DEFAULT) {
            return;
        }
        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED || e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            LangConf.Key.RESOURCEPACK_FAIL.sendMessage(e.getPlayer());
            loadingResourcepackPlayers.remove(e.getPlayer());
        } else if (e.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            resourcepackPlayers.add(e.getPlayer());
            if (loadingResourcepackPlayers.remove(e.getPlayer())) {
                transportPipes.changeRenderSystem(e.getPlayer(), ModelledRenderSystem.getDisplayName());
            }
        }
    }

    public void loadResourcepackForPlayer(Player p) {
        if (getResourcepackMode() == ResourcepackMode.DEFAULT) {
            if (cachedHash == null) {
                p.setResourcePack(URL);
            } else {
                p.setResourcePack(URL, cachedHash);
            }
            loadingResourcepackPlayers.add(p);
        }
    }

    private byte[] calcSHA1Hash(String resourcepackUrl) {
        try {
            URL url = new URL(resourcepackUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getContentLength() <= 0) {
                return null;
            }
            byte[] resourcePackBytes = new byte[connection.getContentLength()];
            InputStream in = connection.getInputStream();

            int b;
            int i = 0;
            while ((b = in.read()) != -1) {
                resourcePackBytes[i] = (byte) b;
                i++;
            }

            in.close();

            if (resourcePackBytes != null) {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                return md.digest(resourcePackBytes);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            sentry.record(e);
        } catch (Exception e) {
            sentry.record(e);
        }
        return null;
    }

    public enum ResourcepackMode {
        DEFAULT, NONE, SERVER
    }

}
