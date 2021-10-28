package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.dropwizard.DropwizardClock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.perpetualnetworks.graphitemetrics.GraphiteMeterRegistryTest.findAvailableUdpPort;

@Slf4j
class GraphiteReporterTest {

    public static final String LOCALHOST = "127.0.0.1";
    private final MockClock mockClock = new MockClock();

    GraphiteConfiguration configuration = GraphiteConfiguration.builder()
            .port(findAvailableUdpPort())
            .prefix("bob")
            .enabled(true)
            .tagsEnabled(true)
            .tags(List.of("bob", "alice"))
            .senderName("sendername")
            .rateUnits(TimeUnit.MILLISECONDS)
            .durationUnits(TimeUnit.SECONDS)
            .step(Duration.ofSeconds(1))
            .stepUnits(ChronoUnit.SECONDS)
            .host(LOCALHOST)
            .executorService(new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    final Thread thread = new Thread(runnable);
                    return thread;
                }
            }))
            .shutdownExecutorOnStop(false)
            .build();

    @Test
    void reporterNoOpTest() {
        GraphiteMeterRegistry registry = new GraphiteMeterRegistry(configuration, mockClock);

        final GraphiteReporter reporter = GraphiteReporterBuilder.forRegistry(registry.getDropwizardRegistry())
                .withGraphiteConfig(configuration)
                .withClock(new DropwizardClock(mockClock))
                .withGraphiteSender(GraphiteSenderFactory.getGraphiteSender(configuration))
                .build();

        reporter.report();

    }

}