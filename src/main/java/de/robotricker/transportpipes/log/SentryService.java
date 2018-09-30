package de.robotricker.transportpipes.log;

import javax.inject.Inject;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.dsn.Dsn;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;

public class SentryService {

    @Inject
    private LoggerService logger;

    private SentryClient client;

    public boolean init(String dsn) {
        try {
            client = Sentry.init(dsn, new SentryClientFactory() {
                @Override
                public SentryClient createSentryClient(Dsn dsn) {
                    return null;
                }
            });
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    public boolean isInitialized() {
        return client != null;
    }

    public void addTag(String name, String value) {
        if (!isInitialized()) {
            return;
        }
        client.getContext().addTag(name, value);
    }

    public void record(Throwable throwable) {
        client.sendException(throwable);
    }

    public void breadcrumb(Breadcrumb.Level level, String category, String message) {
        if (!isInitialized()) {
            return;
        }
        client.getContext().recordBreadcrumb(new BreadcrumbBuilder().setLevel(level).setCategory(category).setMessage(message).build());
    }

    public void injectThread(Thread thread) {
        if (!isInitialized()) {
            return;
        }
        thread.setUncaughtExceptionHandler((currentThread, throwable) -> {
            logger.error("An uncaught error occurred!", throwable);
            record(throwable);
        });
    }

}
