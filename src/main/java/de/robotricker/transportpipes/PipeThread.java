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
import de.robotricker.transportpipes.utils.tickdata.PipeTickData;
import io.sentry.Sentry;

public class PipeThread extends Thread {

	public final static int WANTED_TPS = 10;
	private final static long TICK_DIFF = 1000 / WANTED_TPS;
	private final static int INPUT_ITEMS_TICK_DIFF = 5;
	private final static int VIEW_DISTANCE_TICK_DIFF = 3;

	private final Map<Runnable, Integer> scheduleList = Collections.synchronizedMap(new LinkedHashMap<Runnable, Integer>());

	private int calculatedTps = 0;
	private boolean running = false;

	private long lastTick = 0;
	private int inputItemsTick = 0;
	private int viewDistanceTick = 0;

	private long lastSecond = 0;
	private int tpsCounter = 0;

	public long timeTick = 0;

	private String lastAction = "Starting";

	public PipeThread() {
		super("TransportPipes Thread");
	}

	public int getCalculatedTps() {
		return calculatedTps;
	}

	public String getLastAction() {
		return lastAction;
	}

	public void setLastAction(String lastAction) {
		this.lastAction = lastAction;
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
				lastAction = "Sleeping";
				Thread.sleep(50);
				long currentTime = System.currentTimeMillis();
				lastAction = "TPS calc";
				if (currentTime - lastSecond >= 1000) {
					lastSecond = currentTime;
					calculatedTps = tpsCounter;
					tpsCounter = 0;
				}
				if (currentTime - lastTick < TICK_DIFF) {
					continue;
				}

				lastAction = "Tick checks";
				boolean extractItems = false;
				boolean checkViewDistance = false;

				// input Items tick
				inputItemsTick++;
				if (inputItemsTick == INPUT_ITEMS_TICK_DIFF) {
					inputItemsTick = 0;
					extractItems = true;
				}
				// check view distance tick
				viewDistanceTick++;
				if (viewDistanceTick == VIEW_DISTANCE_TICK_DIFF) {
					viewDistanceTick = 0;
					checkViewDistance = true;
				}

				tpsCounter++;
				lastTick = currentTime;

				// internal PipeThread scheduler. Has nothing to do with the pipes themselves
				lastAction = "Internal scheduler";
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

				// in this list are the items stored which are already processed in this tick
				// (in order to not process an item 2 times in one tick)
				List<PipeItem> itemsTicked = new ArrayList<>();

				// update ducts
				lastAction = "World loop";
				for (World world : Bukkit.getWorlds()) {
					lastAction = "duct map load";
					Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
					if (ductMap != null) {
						synchronized (ductMap) {
							lastAction = "duct loop";
							for (Duct duct : ductMap.values()) {
								if (duct.isInLoadedChunk()) {
									continue;
								}

								lastAction = "Pipe tick";
								duct.tick(new PipeTickData(extractItems, itemsTicked));

							}
						}
					}
				}

				timeTick = (System.nanoTime() - timeBefore);

				if (checkViewDistance) {
					lastAction = "View distance";
					TransportPipes.instance.pipePacketManager.tickSync();
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
		synchronized (scheduleList) {
			scheduleList.put(run, tickDelay);
		}
	}

}
