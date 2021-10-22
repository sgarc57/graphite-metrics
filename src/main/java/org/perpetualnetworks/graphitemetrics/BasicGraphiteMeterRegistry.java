package org.perpetualnetworks.graphitemetrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.codahale.metrics.graphite.PickledGraphite;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.dropwizard.DropwizardClock;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.micrometer.core.lang.Nullable;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteDimensionalNamingConvention;
import io.micrometer.graphite.GraphiteHierarchicalNameMapper;
import io.micrometer.graphite.GraphiteHierarchicalNamingConvention;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class BasicGraphiteMeterRegistry extends DropwizardMeterRegistry {

    private final GraphiteConfig config;
    private final BasicGraphiteReporter reporter;

    //TODO: convert tags to path
    public BasicGraphiteMeterRegistry(GraphiteConfiguration config,
                                      Clock clock) {
        //this(config, clock, config.graphiteTagsEnabled() ? new GraphiteDimensionalNameMapper() : new GraphiteHierarchicalNameMapper(config.tagsAsPrefix()));
        this(config, clock, new GraphiteHierarchicalNameMapper());
    }

    public BasicGraphiteMeterRegistry(GraphiteConfig config,
                                      Clock clock,
                                      HierarchicalNameMapper nameMapper) {
        this(config, clock, nameMapper, new MetricRegistry());
    }

    public BasicGraphiteMeterRegistry(GraphiteConfig config,
                                      Clock clock,
                                      HierarchicalNameMapper nameMapper,
                                      MetricRegistry metricRegistry) {
        this(config, clock, nameMapper, metricRegistry, buildDefaultReporter(config, clock, metricRegistry));
    }

    public BasicGraphiteMeterRegistry(GraphiteConfig config,
                                      Clock clock,
                                      HierarchicalNameMapper nameMapper,
                                      MetricRegistry metricRegistry, BasicGraphiteReporter reporter) {
        super(config, metricRegistry, nameMapper, clock);

        this.config = config;
        config().namingConvention(config.graphiteTagsEnabled() ? new GraphiteDimensionalNamingConvention() : new GraphiteHierarchicalNamingConvention());
        this.reporter = reporter;

        start();
    }

    private static BasicGraphiteReporter buildDefaultReporter(GraphiteConfig config, Clock clock, MetricRegistry metricRegistry) {
        return BasicGraphiteReporterBuilder.forRegistry(metricRegistry)
                .withClock(new DropwizardClock(clock))
                .convertRatesTo(config.rateUnits())
                .convertDurationsTo(config.durationUnits())
                // FIXME: this causes failure if enabled
                //.addMetricAttributesAsTags(config.graphiteTagsEnabled())
                .withGraphiteSender(getGraphiteSender(config))
                .build();
    }

    private static GraphiteSender getGraphiteSender(GraphiteConfig config) {
        switch (config.protocol()) {
            case PLAINTEXT:
                return new Graphite(config.host(), config.port());
            case UDP:
                return new GraphiteUDP(config.host(), config.port());
            case PICKLED:
            default:
                return new PickledGraphite(config.host(), config.port());
        }
    }

    public void stop() {
        if (config.enabled()) {
            log.info("reporter stopped");
            reporter.stop();
        }
    }

    public void start() {
        if (config.enabled()) {
            log.info("reporter started");
            reporter.start(config.step().getSeconds(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void close() {
        if (config.enabled()) {
            log.info("reporter closed");
            reporter.close();
        }
        super.close();
    }

    @Override
    @Nullable
    protected Double nullGaugeValue() {
        return null;
    }
}
