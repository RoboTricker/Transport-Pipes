package de.robotricker.transportpipes.utils.staticutils;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import de.robotricker.transportpipes.TransportPipes;

public class UpdateUtils implements Listener {

	private SpigetUpdate su;

	public UpdateUtils(Plugin plugin) {
		su = new SpigetUpdate(plugin, 20873);
		su.setVersionComparator(new VersionComparator() {

			@Override
			public boolean isNewer(String currentVersion, String checkVersion) {
				long currentVersionLong = convertVersionToLong(currentVersion);
				long checkVersionLong = convertVersionToLong(checkVersion);
				return checkVersionLong > currentVersionLong;
			}
		});
	}

	public void checkForUpdates(final CommandSender cs) {
		su.checkForUpdate(new UpdateCallback() {

			@Override
			public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
				cs.sendMessage("§6There is an update for TransportPipes available: §e" + newVersion);
				cs.sendMessage("§6Download it here: §e" + downloadUrl);
			}

			@Override
			public void upToDate() {

			}
		});
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		if (e.getPlayer().isOp() && TransportPipes.instance.generalConf.isCheckUpdates()) {
			TransportPipes.runTaskLater(new Runnable() {

				@Override
				public void run() {
					checkForUpdates(e.getPlayer());
				}
			}, 60L);
		}
	}

	public static long convertVersionToLong(String version) {
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

	private static int convertMainVersionStringToInt(String mainVersion) {
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
