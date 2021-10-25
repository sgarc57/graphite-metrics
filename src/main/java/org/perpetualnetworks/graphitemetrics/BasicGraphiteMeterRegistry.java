package org.perpetualnetworks.graphitemetrics;

import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.dropwizard.DropwizardClock;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.micrometer.core.lang.Nullable;
import io.micrometer.graphite.GraphiteDimensionalNamingConvention;
import io.micrometer.graphite.GraphiteHierarchicalNameMapper;
import io.micrometer.graphite.GraphiteHierarchicalNamingConvention;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicGraphiteMeterRegistry extends DropwizardMeterRegistry {

    private final GraphiteConfiguration config;
    private final BasicGraphiteReporter reporter;

    //TODO: convert tags to path
    public BasicGraphiteMeterRegistry(GraphiteConfiguration config,
                                      Clock clock) {
        //this(config, clock, config.graphiteTagsEnabled() ? new GraphiteDimensionalNameMapper() : new GraphiteHierarchicalNameMapper(config.tagsAsPrefix()));
        this(config, clock, new GraphiteHierarchicalNameMapper());
    }

    public BasicGraphiteMeterRegistry(GraphiteConfiguration config,
                                      Clock clock,
                                      HierarchicalNameMapper nameMapper) {
        this(config, clock, nameMapper, new MetricRegistry());
    }

    public BasicGraphiteMeterRegistry(GraphiteConfiguration config,
                                      Clock clock,
                                      HierarchicalNameMapper nameMapper,
                                      MetricRegistry metricRegistry) {
        this(config, clock, nameMapper, metricRegistry, buildDefaultReporter(config, clock, metricRegistry));
    }

    public BasicGraphiteMeterRegistry(GraphiteConfiguration config,
                                      Clock clock,
                                      HierarchicalNameMapper nameMapper,
                                      MetricRegistry metricRegistry, BasicGraphiteReporter reporter) {
        super(config, metricRegistry, nameMapper, clock);

        this.config = config;
        config().namingConvention(config.graphiteTagsEnabled() ? new GraphiteDimensionalNamingConvention() : new GraphiteHierarchicalNamingConvention());
        this.reporter = reporter;

        start();
    }

    private static BasicGraphiteReporter buildDefaultReporter(GraphiteConfiguration config, Clock clock, MetricRegistry metricRegistry) {
        return BasicGraphiteReporterBuilder.forRegistry(metricRegistry)
                .withGraphiteConfig(config)
                .withClock(new DropwizardClock(clock))
                //.convertRatesTo(config.rateUnits())
                //.convertDurationsTo(config.durationUnits())
                .withGraphiteSender(BasicGraphiteSenderFactory.getGraphiteSender(config))
                .build();
    }

    public void stop() {
        if (config.enabled()) {
            log.info("registry stopped");
            reporter.stop();
        }
    }

    public void start() {
        if (config.enabled()) {
            log.info("registry started");
            reporter.start(config.getStep().get(config.getStepUnits()), config.getDurationUnits());
        }
    }

    @Override
    public void close() {
        if (config.enabled()) {
            log.info("registry closed");
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
