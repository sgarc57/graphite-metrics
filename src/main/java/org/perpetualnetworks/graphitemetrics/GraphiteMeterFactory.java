package org.perpetualnetworks.graphitemetrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class GraphiteMeterFactory {


    public static void reportTimer(GraphiteReporter reporter, String name, Timer timer, long timestamp) throws IOException {
        Snapshot snapshot = timer.getSnapshot();
        reporter.sendIfEnabled(reporter, MetricAttribute.MAX, name, reporter.convertDuration((double)snapshot.getMax()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.MEAN, name, reporter.convertDuration(snapshot.getMean()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.MIN, name, reporter.convertDuration((double)snapshot.getMin()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.STDDEV, name, reporter.convertDuration(snapshot.getStdDev()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P50, name, reporter.convertDuration(snapshot.getMedian()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P75, name, reporter.convertDuration(snapshot.get75thPercentile()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P95, name, reporter.convertDuration(snapshot.get95thPercentile()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P98, name, reporter.convertDuration(snapshot.get98thPercentile()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P99, name, reporter.convertDuration(snapshot.get99thPercentile()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P999, name, reporter.convertDuration(snapshot.get999thPercentile()), timestamp);
        reportMetered(reporter, name, timer, timestamp);
    }

    public static void reportMetered(GraphiteReporter reporter, String name, Metered meter, long timestamp) throws IOException {
        if (!reporter.getDisabledMetricAttributes().contains(MetricAttribute.COUNT)) {
            reportCounter(reporter, name, meter.getCount(), timestamp);
        }

        reporter.sendIfEnabled(reporter, MetricAttribute.M1_RATE, name, reporter.convertRate(meter.getOneMinuteRate()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.M5_RATE, name, reporter.convertRate(meter.getFiveMinuteRate()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.M15_RATE, name, reporter.convertRate(meter.getFifteenMinuteRate()), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.MEAN_RATE, name, reporter.convertRate(meter.getMeanRate()), timestamp);
    }

    public static void reportHistogram(GraphiteReporter reporter, String name, Histogram histogram, long timestamp) throws IOException {
        Snapshot snapshot = histogram.getSnapshot();
        if (!reporter.getDisabledMetricAttributes().contains(MetricAttribute.COUNT)) {
            reportCounter(reporter, name, histogram.getCount(), timestamp);
        }

        reporter.sendIfEnabled(reporter, MetricAttribute.MAX, name, snapshot.getMax(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.MEAN, name, snapshot.getMean(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.MIN, name, snapshot.getMin(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.STDDEV, name, snapshot.getStdDev(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P50, name, snapshot.getMedian(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P75, name, snapshot.get75thPercentile(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P95, name, snapshot.get95thPercentile(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P98, name, snapshot.get98thPercentile(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P99, name, snapshot.get99thPercentile(), timestamp);
        reporter.sendIfEnabled(reporter, MetricAttribute.P999, name, snapshot.get999thPercentile(), timestamp);
    }

    public static void reportGauge(GraphiteReporter reporter, String name, Gauge<?> gauge, long timestamp) throws IOException {
        String value = FormatUtils.formatObject(gauge.getValue());
        if (value != null) {
            reporter.getSender().send(reporter.getPrefix(name), value, timestamp);
        }
    }

    public static void reportCounter(GraphiteReporter reporter, String name, Counter counter, long timestamp) throws IOException {
        reportCounter(reporter, name, counter.getCount(), timestamp);
    }

    public static void reportCounter(GraphiteReporter reporter, String name, long value, long timestamp) throws IOException {
        reporter.getSender().send(reporter.getPrefix(name, MetricAttribute.COUNT.getCode()), FormatUtils.format(value), timestamp);
        long diff = value - (Long) Optional.ofNullable((Long)reporter.getReportedCounters().put(name, value)).orElse(0L);
        if (diff != 0L) {
            reporter.getSender().send(reporter.getPrefix(name, "hits"), FormatUtils.format(diff), timestamp);
            reporter.getSender().send(reporter.getPrefix(name, "cps"), FormatUtils.format((double)diff * reporter.getCountFactor()), timestamp);
        }

    }
}
