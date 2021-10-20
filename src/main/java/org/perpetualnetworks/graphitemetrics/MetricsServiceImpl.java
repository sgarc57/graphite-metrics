package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;

public class MetricsServiceImpl implements MetricsService {

    private final BasicGraphiteMeterRegistry basicGraphiteMeterRegistry;

    public MetricsServiceImpl(GraphiteConfiguration graphiteConfiguration) {
        basicGraphiteMeterRegistry = new BasicGraphiteMeterRegistry(graphiteConfiguration, Clock.SYSTEM);
        Metrics.addRegistry(basicGraphiteMeterRegistry);
    }

    //TODO: check where needed
    public void close() {
        basicGraphiteMeterRegistry.close();
        basicGraphiteMeterRegistry.stop();
    }
}
