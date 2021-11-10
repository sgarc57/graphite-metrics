package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public interface MetricsService {

    public void start();

    public void stop();

    public void close();

    public MeterRegistry getMeterRegistry();

    public Timer fetchTimer(String timerName);

    public Counter fetchCounter(String counterName);

    public Gauge fetchGauge(String gaugename);

    public LongTaskTimer fetchLongTaskTimer(String longTaskTimerName);
}
