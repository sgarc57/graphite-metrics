package org.perpetualnetworks.graphitemetrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteSender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BasicGraphiteReporterBuilder {
    @AllArgsConstructor
    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        @Getter
        @Setter
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        @Getter
        @Setter
        private MetricFilter filter = MetricFilter.ALL;
        @Getter
        @Setter
        private ScheduledExecutorService executor;
        @Getter
        @Setter
        private Boolean shutdownExecutorOnStop = true;
        @Getter
        @Setter
        private Set<MetricAttribute> disabledMetricAttributes;
        @With
        @Getter
        private GraphiteSender graphiteSender;

        private GraphiteConfiguration graphiteConfiguration;

        Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.disabledMetricAttributes = Collections.emptySet();
        }

        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withGraphiteConfig(GraphiteConfiguration graphiteConfiguration) {
            this.graphiteConfiguration = graphiteConfiguration;
            return this;
        }

        //public Builder prefixedWith(String prefix) {
        //    this.prefix = prefix;
        //    return this;
        //}

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        // public Builder filter(MetricFilter filter) {
        //     this.filter = filter;
        //     return this;
        // }

        //public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
        //    this.disabledMetricAttributes = disabledMetricAttributes;
        //    return this;
        //}

        public BasicGraphiteReporter build(Graphite graphite) {
            withGraphiteSender(graphite);
            return this.build();
        }

        public BasicGraphiteReporter build() {
            if (this.graphiteSender == null) {
                throw new IllegalArgumentException("graphite sender can not be null");
            }
            if (this.registry == null) {
                throw new IllegalArgumentException("registry can not be null");
            }
            if (this.graphiteConfiguration == null) {
                throw new IllegalArgumentException("graphite config can not be null");
            }
            return new BasicGraphiteReporter(
                    this.registry,
                    graphiteSender,
                    this.clock,
                    //TODO: fix
                    this.graphiteConfiguration,
                    //this.prefix, this.rateUnit, this.durationUnit,
                    getFilter(),
                    //this.executor,
                    //getExecutor(),
                    //this.shutdownExecutorOnStop,
                    //getShutdownExecutorOnStop(),
                    getDisabledMetricAttributes());
        }
    }

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }
}
