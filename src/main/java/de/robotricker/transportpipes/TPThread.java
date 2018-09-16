package de.robotricker.transportpipes;

import io.sentry.Sentry;

public class TPThread extends Thread {

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

}
