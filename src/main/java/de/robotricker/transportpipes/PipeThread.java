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

import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class PipeThread extends Thread {

	public final static int WANTED_TPS = 10;

	private final static long TICK_DIFF = 1000 / WANTED_TPS;
	private final static int INPUT_ITEMS_TICK_DIFF = 3;
	private final static int VIEW_DISTANCE_TICK_DIFF = 3;

	//jedes iteraten durch diese Map MUSS mit synchronized(tickList){} sein!
	private static final Map<Runnable, Integer> tickList = Collections.synchronizedMap(new LinkedHashMap<Runnable, Integer>());

	private static int calculatedTps = 0;
	private static boolean running = false;

	private long lastTick = 0;
	private int inputItemsTick = 0;
	private int viewDistanceTick = 0;

	private long lastSecond = 0;
	private int tpsCounter = 0;

	public static long timeTick = 0;

	private static String lastAction = "Starting";

	public PipeThread() {
		super("TransportPipes Thread");
	}

	public static int getCalculatedTps() {
		return calculatedTps;
	}

	public static String getLastAction() {
		return lastAction;
	}

	public static void setLastAction(String lastAction) {
		PipeThread.lastAction = lastAction;
	}

	public long getLastTickDiff() {
		return System.currentTimeMillis() - lastTick;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		PipeThread.running = running;
	}

	@Override
	public void run() {
		System.out.println("starting TransportPipes-Thread");
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

				//input Items tick
				inputItemsTick++;
				if (inputItemsTick == INPUT_ITEMS_TICK_DIFF) {
					inputItemsTick = 0;
					extractItems = true;
				}
				//check view distance tick
				viewDistanceTick++;
				if (viewDistanceTick == VIEW_DISTANCE_TICK_DIFF) {
					viewDistanceTick = 0;
					checkViewDistance = true;
				}

				tpsCounter++;
				lastTick = currentTime;

				//internal PipeThread scheduler. Has nothing to do with the pipes themselves
				lastAction = "Internal scheduler";
				{
					HashMap<Runnable, Integer> tempTickList = new HashMap<>();
					synchronized (tickList) {
						tempTickList.putAll(tickList);
					}
					for (Runnable r : tempTickList.keySet()) {
						if (r != null) {
							int v = tempTickList.get(r);
							if (v == 0) {
								tickList.remove(r);
								r.run();
							} else {
								v--;
								tickList.put(r, v);
							}
						}
					}
				}

				long timeBefore = System.nanoTime();

				//in this list are the items stored which are already processed in this tick (in order to not process an item 2 times in one tick)
				List<PipeItem> itemsTicked = new ArrayList<>();

				//update pipes
				lastAction = "World loop";
				for (World world : Bukkit.getWorlds()) {
					lastAction = "Pipe map load";
					Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
					if (pipeMap != null) {
						synchronized (pipeMap) {
							lastAction = "Pipe loop";
							for (Pipe pipe : pipeMap.values()) {
								//insert items from "tempPipeItemsWithSpawn"
								synchronized (pipe.tempPipeItemsWithSpawn) {
									Iterator<PipeItem> itemIterator = pipe.tempPipeItemsWithSpawn.keySet().iterator();
									while (itemIterator.hasNext()) {
										PipeItem pipeItem = itemIterator.next();

										PipeDirection dir = pipe.tempPipeItemsWithSpawn.get(pipeItem);
										pipe.putPipeItem(pipeItem, dir);

										TransportPipes.pipePacketManager.createPipeItem(pipeItem);

										itemIterator.remove();
									}
								}

								//put the "tempPipeItems" which had been put there by the tick method in the pipe before, into the "pipeItems" where they got affected by the tick method
								synchronized (pipe.tempPipeItems) {
									Iterator<PipeItem> itemIterator = pipe.tempPipeItems.keySet().iterator();
									while (itemIterator.hasNext()) {
										PipeItem pipeItem = itemIterator.next();

										if (!itemsTicked.contains(pipeItem)) {
											PipeDirection dir = pipe.tempPipeItems.get(pipeItem);
											pipe.putPipeItem(pipeItem, dir);
											itemIterator.remove();
										}
									}
								}

								//tick pipe (move the pipe items etc.)
								lastAction = "Pipe tick";
								pipe.tick(extractItems, itemsTicked);

							}
						}
					}
				}

				PipeThread.timeTick = (System.nanoTime() - timeBefore);

				if (checkViewDistance) {
					lastAction = "View distance";
					TransportPipes.pipePacketManager.tickSync();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ConcurrentModificationException e) {
				handleAsyncError(e);
			}
		}
		System.out.println("stopping TransportPipes-Thread");

	}

	public static void runTask(Runnable run, int tickDelay) {
		synchronized (tickList) {
			tickList.put(run, tickDelay);
		}
	}

	public static void handleAsyncError(Exception e) {
		System.err.println("------------------------> ASYNC ERROR: <------------------------");
		e.printStackTrace();
	}

}
