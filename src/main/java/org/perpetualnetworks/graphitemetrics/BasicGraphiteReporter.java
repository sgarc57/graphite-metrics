package org.perpetualnetworks.graphitemetrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.GraphiteSender;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BasicGraphiteReporter extends ScheduledReporter {
    @Getter
    private final Map<String, Long> reportedCounters = new ConcurrentHashMap<>();
    @Getter
    private volatile double countFactor = 1.0D;
    @Getter
    private final GraphiteSender sender;
    private final Clock clock;
    private final String prefix;


    //TODO: fix builder method(s)
    protected BasicGraphiteReporter(MetricRegistry registry,
                                    GraphiteSender sender,
                                    Clock clock,
                                    // String prefix,
                                    // TimeUnit rateUnit,
                                    // TimeUnit durationUnit,
                                    GraphiteConfiguration graphiteConfiguration,
                                    MetricFilter filter,
                                    ScheduledExecutorService executorService,
                                    Boolean shutdownExecutorOnStop,
                                    Set<MetricAttribute> disabledMetricAttributes) {
        //super(registry, "perpetual-graphite-reporter", filter, rateUnit, durationUnit, executorService, shutdownExecutorOnStop, disabledMetricAttributes);
        super(registry, graphiteConfiguration.getSenderName(), filter, graphiteConfiguration.getRateUnits(), graphiteConfiguration.getDurationUnits());
        this.sender = sender;
        this.clock = clock;
        this.prefix = graphiteConfiguration.getPrefix();
        //this.prefix = prefix;
    }

    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        //FOR DEBUGGING:
        //log.info("starting report with \n gauges: " + gauges + "\n counters: " + counters + "\n meters: " + meters
        //      + "\n timers: " + timers);

        long timestamp = getTimestamp();

        try {
            this.sender.connect();
            Iterator var8 = gauges.entrySet().iterator();

            Entry entry;
            while (var8.hasNext()) {
                entry = (Entry) var8.next();
                BasicGraphiteReportSenderFactory.reportGauge(this, (String) entry.getKey(), (Gauge) entry.getValue(), timestamp);
            }

            var8 = counters.entrySet().iterator();

            while (var8.hasNext()) {
                entry = (Entry) var8.next();
                BasicGraphiteReportSenderFactory.reportCounter(this, (String) entry.getKey(), (Counter) entry.getValue(), timestamp);
            }

            var8 = histograms.entrySet().iterator();

            while (var8.hasNext()) {
                entry = (Entry) var8.next();
                BasicGraphiteReportSenderFactory.reportHistogram(this, (String) entry.getKey(), (Histogram) entry.getValue(), timestamp);
            }

            var8 = meters.entrySet().iterator();

            while (var8.hasNext()) {
                entry = (Entry) var8.next();
                //log.info("reporting meter entry: " + entry.getKey() + " " + entry.getValue().toString());
                //log.info("timestamp was: " + timestamp);
                BasicGraphiteReportSenderFactory.reportMetered(this, (String) entry.getKey(), (Metered) entry.getValue(), timestamp);
            }

            var8 = timers.entrySet().iterator();

            while (var8.hasNext()) {
                entry = (Entry) var8.next();
                //log.info("reporting timer entry: " + entry.getKey() + " " + entry.getValue());
                BasicGraphiteReportSenderFactory.reportTimer(this, (String) entry.getKey(), (Timer) entry.getValue(), timestamp);
            }

            this.sender.flush();
        } catch (IOException var18) {
            toss("Unable to report to Graphite", this.sender, var18);
        } finally {
            try {
                this.sender.close();
            } catch (IOException var17) {
                toss("Error closing Graphite", this.sender, var17);
            }

        }

    }

    @Override
    protected double convertDuration(double duration) {
        return super.convertDuration(duration);
    }

    @Override
    protected double convertRate(double rate) {
        return super.convertRate(rate);
    }

    @Override
    protected Set<MetricAttribute> getDisabledMetricAttributes() {
        return super.getDisabledMetricAttributes();
    }

    private long getTimestamp() {
        return this.clock.getTime() / 1000L;
    }

    private void toss(String msg, GraphiteSender sender, Exception e) {
        log.error(String.format("error during sending: message %s, object %s", msg, sender), e);
    }

    public void stop() {
        try {
            super.stop();
        } finally {
            try {
                this.sender.close();
            } catch (IOException var7) {
                toss("Error disconnecting from Graphite", this.sender, var7);
            }
        }
    }

    public void sendIfEnabled(BasicGraphiteReporter reporter, MetricAttribute type, String name, double value, long timestamp) throws IOException {
        if (!reporter.getDisabledMetricAttributes().contains(type)) {
            reporter.sender.send(reporter.getPrefix(name, type.getCode()), FormatUtils.format(value), timestamp);
        }
    }

    public void sendIfEnabled(BasicGraphiteReporter reporter, MetricAttribute type, String name, long value, long timestamp) throws IOException {
        //FOR DEBUGGING:
        //log.info("evaluating send if enabled with disabled attributes: " + reporter.getDisabledMetricAttributes());
        if (!reporter.getDisabledMetricAttributes().contains(type)) {
            final String prefix = reporter.getPrefix(name, type.getCode());
            final String format = FormatUtils.format(value);
            log.info("sending data to prefix: " + prefix + " with value: " + format);
            reporter.sender.send(prefix, format, timestamp);
        }
    }

    public String getPrefix(String... components) {
        return MetricRegistry.name(this.prefix, components);
    }

    public void start(long period, TimeUnit unit) {
        this.countFactor = 1.0D / (double) unit.toMillis(period) * 1000.0D;
        super.start(period, unit);
    }

}
