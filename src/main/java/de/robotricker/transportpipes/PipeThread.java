package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class PipeThread extends Thread {

	public final static int WANTED_TPS = 7;

	private final static long TICK_DIFF = 1000 / WANTED_TPS;
	private final static int INPUT_ITEMS_TICK_DIFF = 3;
	private final static int VIEW_DISTANCE_TICK_DIFF = 3;

	//jedes iteraten durch diese Map MUSS mit synchronized(tickList){} sein!
	private static final Map<Runnable, Integer> tickList = Collections.synchronizedMap(new HashMap<Runnable, Integer>());

	private static int calculatedTps = 0;
	private static boolean running = false;

	private long lastTick = 0;
	private int inputItemsTick = 0;
	private int viewDistanceTick = 0;

	private long lastSecond = 0;
	private int tpsCounter = 0;

	public static long timeTick = 0;

	public PipeThread() {
		super("TransportPipes Thread");
	}

	public static int getCalculatedTps() {
		return calculatedTps;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		PipeThread.running = running;
	}

	@Override
	public void run() {
		System.out.println(TransportPipes.instance.PREFIX_CONSOLE + "starting TransportPipes-Thread");
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

				boolean inputItems = false;
				boolean checkViewDistance = false;

				//input Items tick
				inputItemsTick++;
				if (inputItemsTick == INPUT_ITEMS_TICK_DIFF) {
					inputItemsTick = 0;
					inputItems = true;
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

				//in this list are the items stored which are already processed in this tick (that no item is processed 2 times in 1 tick)
				List<PipeItem> roundItems = new ArrayList<>();

				//update pipes
				for (World world : Bukkit.getWorlds()) {
					Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(world);
					if (pipeMap != null) {
						synchronized (pipeMap) {
							for (Pipe pipe : pipeMap.values()) {
								//input items from "tempPipeItemsWithSpawn"
								if (inputItems) {
									synchronized (pipe.tempPipeItemsWithSpawn) {
										Iterator<PipeItem> itemIterator = pipe.tempPipeItemsWithSpawn.keySet().iterator();
										while (itemIterator.hasNext()) {
											PipeItem pipeItem = itemIterator.next();

											PipeDirection dir = pipe.tempPipeItemsWithSpawn.get(pipeItem);
											pipe.putPipeItem(pipeItem, dir);

											TransportPipes.pipePacketManager.spawnPipeItemSync(pipeItem);

											itemIterator.remove();

										}
									}
								}

								//put the "tempPipeItems" which had been put there by the tick method in the pipe before, into the "pipeItems" where they got affected by the tick method
								synchronized (pipe.tempPipeItems) {
									Iterator<PipeItem> itemIterator = pipe.tempPipeItems.keySet().iterator();
									while (itemIterator.hasNext()) {
										PipeItem pipeItem = itemIterator.next();

										if (!roundItems.contains(pipeItem)) {
											PipeDirection dir = pipe.tempPipeItems.get(pipeItem);
											pipe.putPipeItem(pipeItem, dir);
											itemIterator.remove();
										}
									}
								}

								//tick pipe (move the pipe items etc.)
								pipe.tick(inputItems, roundItems);

							}
						}
					}
				}

				PipeThread.timeTick = (System.nanoTime() - timeBefore);

				if (checkViewDistance) {
					TransportPipes.pipePacketManager.tickSync();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(TransportPipes.instance.PREFIX_CONSOLE + "finishing TransportPipes-Thread");

	}

	public static void runTask(Runnable run, int tickDelay) {
		tickList.put(run, tickDelay);
	}

}
