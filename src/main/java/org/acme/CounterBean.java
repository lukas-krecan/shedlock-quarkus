package org.acme;

import io.quarkus.scheduler.Scheduled;
import net.javacrumbs.quarkus.SchedulerLock;
import net.javacrumbs.shedlock.core.LockAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class CounterBean {
    private static final Logger logger = LoggerFactory.getLogger(CounterBean.class);

    private final AtomicInteger counter = new AtomicInteger();

    public int get() {
        return counter.get();
    }

    @Scheduled(every="10s")
    @SchedulerLock
    void increment() {
        LockAssert.assertLocked();
        counter.incrementAndGet();
        logger.info("Tick");
    }
}
