package org.perpetualnetworks.graphitemetrics;


import io.micrometer.core.instrument.config.validate.Validated;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteProtocol;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Value
@Builder(toBuilder = true)
public class GraphiteConfiguration implements GraphiteConfig {
    @Getter
    String senderName;
    @Getter
    String host;
    @Getter
    Integer port;
    @Getter
    Boolean enabled;
    @Getter
    String prefix;  //this will be the first dotted segment to the metric key
    @Getter
    List<String> tags;
    @Getter
    @Setter
    Boolean tagsEnabled;
    @Getter
    @Setter
    @NonNull
    Duration step; // duration / period of time between reports

    @Getter
    @Setter
    @NonNull
    TemporalUnit stepUnits; //units for steps

    TimeUnit rateUnits;

    @Getter
    @Setter
    TimeUnit durationUnits;

    @Getter
    @Setter
    @NonNull
    ScheduledExecutorService executorService;
    @Getter
    @Setter
    Boolean shutdownExecutorOnStop;


    @Override
    public String get(String key) {
        return key;
    }

    @Override
    public String prefix() {
        return getPrefix();
    }

    @Override
    public boolean graphiteTagsEnabled() {
        return getTagsEnabled();
    }

    @Override
    public String[] tagsAsPrefix() {
        return getTags().toArray(new String[0]);
    }

    @Override
    public TimeUnit rateUnits() {
        return getRateUnits();
    }

    @Override
    public TimeUnit durationUnits() {
        return getDurationUnits();
    }

    @Override
    public String host() {
        return getHost();
    }

    @Override
    public int port() {
        return getPort();
    }

    @Override
    public boolean enabled() {
        return getEnabled();
    }

    @Override
    public GraphiteProtocol protocol() {
        //default protocol for graphite
        return GraphiteProtocol.UDP;
    }

    @Override
    public Duration step() {
        return getStep();
    }

    @Override
    public Validated<?> validate() {
        return GraphiteConfig.super.validate();
    }

    @Override
    public void requireValid() throws ValidationException {
        validate();
    }
}
