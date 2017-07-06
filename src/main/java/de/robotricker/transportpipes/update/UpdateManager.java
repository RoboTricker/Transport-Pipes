package de.robotricker.transportpipes.update;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import de.robotricker.transportpipes.TransportPipes;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UpdateManager implements Listener {

	private SpigetUpdate su;

	public UpdateManager(Plugin plugin) {
		su = new SpigetUpdate(plugin, 20873);
		su.setVersionComparator(VersionComparator.SEM_VER);
	}

	public void checkForUpdates(final CommandSender cs) {
		su.checkForUpdate(new UpdateCallback() {

			@Override
			public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m---------------&7&l[ &6TransportPipes " + TransportPipes.instance.getDescription().getVersion() + "&7&l]&7&l&m---------------"));
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Update available: &e" + newVersion));

				TextComponent click = new TextComponent("Click");
				click.setColor(net.md_5.bungee.api.ChatColor.GOLD);
				TextComponent here = new TextComponent(" here ");
				here.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
				here.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpipes update"));
				here.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to download the latest version").create()));
				TextComponent toUpdate = new TextComponent("to update the plugin");
				toUpdate.setColor(net.md_5.bungee.api.ChatColor.GOLD);

				cs.spigot().sendMessage(new TextComponent(click, here, toUpdate));
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m--------------------------------------------"));
			}

			@Override
			public void upToDate() {
				
			}
		});
	}

	public void updatePlugin(CommandSender cs) {
		if (su.downloadUpdate()) {
			cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Update successful. &oRestart the server to complete the update process."));
		} else {
			cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUpdate failed: " + su.getFailReason()));
		}
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		if (e.getPlayer().hasPermission(TransportPipes.instance.getConfig().getString("permissions.update", "tp.update"))) {
			Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {

				@Override
				public void run() {
					TransportPipes.updateManager.checkForUpdates(e.getPlayer());
				}
			}, 60L);
		}
	}

}
