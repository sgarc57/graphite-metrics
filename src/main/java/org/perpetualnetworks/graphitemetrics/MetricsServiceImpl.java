package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

public class MetricsServiceImpl implements MetricsService {

    private final GraphiteMeterRegistry graphiteMeterRegistry;

    public MetricsServiceImpl(GraphiteConfiguration graphiteConfiguration) {
        graphiteMeterRegistry = new GraphiteMeterRegistry(graphiteConfiguration, Clock.SYSTEM);
        Metrics.addRegistry(graphiteMeterRegistry);
    }

    public GraphiteMeterRegistry getGraphiteMeterRegistry() {
        return this.graphiteMeterRegistry;
    }

    @Override
    public void start() {
        getGraphiteMeterRegistry().start();
    }

    @Override
    public void close() {
        getGraphiteMeterRegistry().close();
        stop();
    }

    @Override
    public MeterRegistry getMeterRegistry() {
        return getGraphiteMeterRegistry();
    }

    @Override
    public void stop() {
        getGraphiteMeterRegistry().stop();
    }

    @Override
    public Timer fetchTimer(String timerName) {
        return getGraphiteMeterRegistry().get(timerName).timer();
    }

    @Override
    public Counter fetchCounter(String counterName) {
        return getGraphiteMeterRegistry().get(counterName).counter();
    }

    @Override
    public Gauge fetchGauge(String gaugename) {
        return getGraphiteMeterRegistry().get(gaugename).gauge();
    }

    @Override
    public LongTaskTimer fetchLongTaskTimer(String longTaskTimerName) {
        return getGraphiteMeterRegistry().get(longTaskTimerName).longTaskTimer();
    }
}
