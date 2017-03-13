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

	public static int WANTED_TPS = 7;
	public static int CALCULATED_TPS = 0;

	//jedes iteraten durch diese Map MUSS mit synchronized(tickList){} sein!
	public static Map<Runnable, Integer> tickList = Collections.synchronizedMap(new HashMap<Runnable, Integer>());

	public static long tickDiff = 1000 / WANTED_TPS;
	private int inputItemsTickDiff = 3;
	private int viewDistanceTickDiff = 3;

	private long lastTick = 0;
	private int inputItemsTick = 0;
	private int viewDistanceTick = 0;

	private long lastSecond = 0;
	private int tpsCounter = 0;

	public static boolean running = false;

	public static long timeTick = 0;

	public PipeThread() {
		super("TransportPipes Thread");
	}
	
	@Override
	public void run() {
		System.out.println(TransportPipes.PREFIX_CONSOLE + "starting TransportPipes-Thread");
		while (running) {

			long currentTime = System.currentTimeMillis();
			if (currentTime - lastSecond >= 1000) {
				lastSecond = currentTime;
				CALCULATED_TPS = tpsCounter;
				tpsCounter = 0;
			}
			if (currentTime - lastTick < tickDiff) {
				continue;
			}
			
			boolean inputItems = false;
			boolean checkViewDistance = false;
			
			//input Items tick
			inputItemsTick++;
			if (inputItemsTick == inputItemsTickDiff) {
				inputItemsTick = 0;
				inputItems = true;
			}
			//check view distance tick
			viewDistanceTick++;
			if (viewDistanceTick == viewDistanceTickDiff) {
				viewDistanceTick = 0;
				checkViewDistance = true;
			}

			tpsCounter++;
			lastTick = currentTime;

			//internal PipeThread scheduler. Has nothing to do with the pipes themselves
			{
				HashMap<Runnable, Integer> tempTickList = new HashMap<Runnable, Integer>();
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
			List<PipeItem> roundItems = new ArrayList<PipeItem>();

			//update pipes
			for (World world : Bukkit.getWorlds()) {
				Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(world);
				if (pipeMap != null) {
					synchronized (pipeMap) {
						Iterator<Pipe> iterator = pipeMap.values().iterator();
						while (iterator.hasNext()) {
							Pipe pipe = iterator.next();

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

		}
		System.out.println(TransportPipes.PREFIX_CONSOLE + "finishing TransportPipes-Thread");
	}

	public static void runTask(Runnable run, int tickDelay) {
		tickList.put(run, tickDelay);
	}

}
