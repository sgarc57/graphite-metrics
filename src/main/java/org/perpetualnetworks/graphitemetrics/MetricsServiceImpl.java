package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;

public class MetricsServiceImpl implements MetricsService {

    private final GraphiteMeterRegistry graphiteMeterRegistry;

    public MetricsServiceImpl(GraphiteConfiguration graphiteConfiguration) {
        graphiteMeterRegistry = new GraphiteMeterRegistry(graphiteConfiguration, Clock.SYSTEM);
        Metrics.addRegistry(graphiteMeterRegistry);
    }

    //TODO: check where needed
    public void close() {
        graphiteMeterRegistry.close();
        graphiteMeterRegistry.stop();
    }
}
