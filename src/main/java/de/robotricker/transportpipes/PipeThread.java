package de.robotricker.transportpipes;

import de.robotricker.transportpipes.log.LoggerService;
import de.robotricker.transportpipes.log.SentryService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PipeThread extends Thread {

    private final LoggerService logger;
    private final SentryService sentry;

    private final Map<Runnable, Long> tasks;
    private boolean running = false;
    private int preferredTPS = 10;
    private int currentTPS = 0;

    @Inject
    PipeThread(LoggerService logger, SentryService sentry) {
        super("TransportPipes-Thread");
        this.logger = logger;
        this.sentry = sentry;
        tasks = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    @Override
    public void run() {
        logger.info("Started TPThread");
        running = true;
        sentry.addTag("thread", getName());
        sentry.injectThread(this);

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
                    logger.debug("TPS: " + currentTPS);
                }

            } else {
                long waitTime = (long) (1000f / preferredTPS - diff);
                try {
                    sleep(waitTime);
                } catch (InterruptedException e) {
                    logger.error("TPThread was terminated while sleeping!", e);
                }
            }
        }
        logger.info("Stopped TPThread");
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
