package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;

public class MetricsServiceImpl implements MetricsService {

    private final BasicGraphiteMeterRegistry basicGraphiteMeterRegistry;

    public MetricsServiceImpl(GraphiteConfiguration graphiteConfiguration) {
        Clock clock = new Clock() {
            @Override
            public long wallTime() {
                return System.currentTimeMillis();
            }

            @Override
            public long monotonicTime() {
                return SYSTEM.monotonicTime();
            }
        };
        basicGraphiteMeterRegistry = new BasicGraphiteMeterRegistry(graphiteConfiguration.toGraphiteConfig(), clock);
        Metrics.addRegistry(basicGraphiteMeterRegistry);

    }

    //TODO: check where needed
    public void close() {
        basicGraphiteMeterRegistry.close();
        basicGraphiteMeterRegistry.stop();
    }
}
