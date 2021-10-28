package org.perpetualnetworks.graphitemetrics;

import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.codahale.metrics.graphite.PickledGraphite;

public class GraphiteSenderFactory {
    public static GraphiteSender getGraphiteSender(GraphiteConfiguration config) {
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

}
