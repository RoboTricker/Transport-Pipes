package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.IllegalPluginAccessException;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.tick.PipeTickData;
import io.sentry.Sentry;

public class PipeThread extends Thread {

	public final static int WANTED_TPS = 10;
	private final static long TICK_DIFF = 1000 / WANTED_TPS;
	public final static int EXTRACT_ITEMS_TICK_DIFF = 5;
	public final static int VIEW_DISTANCE_TICK_DIFF = 3;

	private final Map<Runnable, Integer> scheduleList = Collections.synchronizedMap(new LinkedHashMap<Runnable, Integer>());

	private int calculatedTps = 0;
	private boolean running = false;

	private long lastTick = 0;

	private long lastSecond = 0;
	private int tpsCounter = 0;

	public long timeTick = 0;

	private long numberOfTicksSinceStart = 0;

	public PipeThread() {
		super("TransportPipes Thread");
	}

	public int getCalculatedTps() {
		return calculatedTps;
	}

	public long getLastTickDiff() {
		return System.currentTimeMillis() - lastTick;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {

		TransportPipes.initSentryOnCurrentThread();

		TransportPipes.instance.getLogger().info("starting TransportPipes-Thread");
		while (running) {

			try {
				Thread.sleep(50);
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastSecond >= 1000) {
					lastSecond = currentTime;
					calculatedTps = tpsCounter;
					tpsCounter = 0;
				}
				if (currentTime - lastTick < TICK_DIFF) {
					continue;
				}

				tpsCounter++;
				lastTick = currentTime;
				numberOfTicksSinceStart++;

				// internal PipeThread scheduler. This is not related to the pipes themselves
				{
					HashMap<Runnable, Integer> tempTickList = new HashMap<>();
					synchronized (scheduleList) {
						tempTickList.putAll(scheduleList);
					}
					for (Runnable r : tempTickList.keySet()) {
						if (r != null) {
							int v = tempTickList.get(r);
							if (v == 0) {
								scheduleList.remove(r);
								r.run();
							} else {
								v--;
								scheduleList.put(r, v);
							}
						}
					}
				}

				long timeBefore = System.nanoTime();

				for (DuctType ductType : DuctType.values()) {
					if (!ductType.isEnabled()) {
						continue;
					}
					ductType.runTickRunnable(numberOfTicksSinceStart);
				}

				timeTick = (System.nanoTime() - timeBefore);

				// update ducts based on view distance
				if (numberOfTicksSinceStart % VIEW_DISTANCE_TICK_DIFF == 0) {
					TransportPipes.instance.ductManager.tickSync();
				}

			} catch (IllegalPluginAccessException e) {
				// do nothing when TP tries to register a scheduler but is already disabled
			} catch (Exception e) {
				e.printStackTrace();
				Sentry.capture(e);
			}
		}
		TransportPipes.instance.getLogger().info("stopping TransportPipes-Thread");

	}

	public void runTask(Runnable run, int tickDelay) {
		scheduleList.put(run, tickDelay);
	}

}
