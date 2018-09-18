package de.robotricker.transportpipes;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import io.sentry.Sentry;

public class TPThread extends Thread {

    private final Map<Runnable, Long> tasks = Collections.synchronizedMap(new LinkedHashMap<>());
    private boolean running = false;
    private int preferredTPS = 10;
    private int currentTPS = 0;

    public TPThread() {
        super("TransportPipes-Thread");
    }

    @Override
    public void run() {
        TransportPipes.logInfo("Started TPThread");
        running = true;
        Sentry.getContext().addTag("thread", Thread.currentThread().getName());
        setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            Sentry.capture(e);
        });

        long lastTick = System.currentTimeMillis();
        long lastSec = lastTick;
        int tpsCounter = 0;
        while (running) {
            long currentTick = System.currentTimeMillis();
            long diff = currentTick - lastTick;
            if (diff >= 1000f / preferredTPS) {

                tick();

                tpsCounter++;
                lastTick = currentTick;

                if (currentTick - lastSec >= 1000) {
                    currentTPS = tpsCounter;
                    tpsCounter = 0;
                    lastSec = currentTick;
                    TransportPipes.logDebug("TPS: " + currentTPS);
                }

            } else {
                long waitTime = (long) (1000f / preferredTPS - diff);
                try {
                    sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        TransportPipes.logInfo("Stopped TPThread");
    }

    private void tick() {
        //schedule tasks
        synchronized (tasks) {
            Iterator<Runnable> taskIt = tasks.keySet().iterator();
            while (taskIt.hasNext()) {
                Runnable task = taskIt.next();
                if (tasks.get(task) > 1) {
                    tasks.put(task, tasks.get(task) - 1);
                } else {
                    taskIt.remove();
                    task.run();
                }
            }
        }
    }

    public int getCurrentTPS() {
        return currentTPS;
    }

    public int getPreferredTPS() {
        return preferredTPS;
    }

    public void setPreferredTPS(int preferredTPS) {
        this.preferredTPS = preferredTPS;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopRunning() {
        running = false;
    }

    public void runTask(Runnable runnable, long delay) {
        tasks.put(runnable, delay);
    }
}
