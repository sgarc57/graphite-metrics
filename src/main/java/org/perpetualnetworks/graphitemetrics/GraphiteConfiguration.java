package org.perpetualnetworks.graphitemetrics;


import io.micrometer.core.instrument.config.validate.Validated;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteProtocol;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Value
@Builder
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
    String prefix;
    @Getter
    List<String> tags;
    @Getter
    Boolean tagsEnabled = false;
    @Getter
    Duration step;

    TimeUnit rateUnits;

    TimeUnit durationUnits;

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
    public int port() {
        return getPort();
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
