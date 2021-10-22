package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.MockClock;
import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.net.DatagramSocket;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicGraphiteReporterTest {

    public static final String LOCALHOST = "localhost";
    private final MockClock mockClock = new MockClock();

    GraphiteConfiguration configuration = GraphiteConfiguration.builder()
            .port(findAvailableUdpPort())
            .prefix("bob")
            .rateUnits(TimeUnit.SECONDS)
            .durationUnits(TimeUnit.SECONDS)
            .step(Duration.ofSeconds(1))
            .host(LOCALHOST)
            .build();

    @Test
    void metricPrefixes() throws InterruptedException {
        final CountDownLatch receiveLatch = new CountDownLatch(1);
        BasicGraphiteMeterRegistry registry = new BasicGraphiteMeterRegistry(configuration, mockClock);

        Connection server = UdpServer.create()
                .option(ChannelOption.SO_REUSEADDR, true)
                .host(LOCALHOST)
                .port(configuration.getPort())
                .handle((in, out) -> {
                    in.receive()
                            .asString()
                            .subscribe(line -> {
                                assertTrue(line.startsWith("my.timer"));
                                receiveLatch.countDown();
                            });
                    return Flux.never();
                })
                .bind()
                .doOnSuccess(v -> {
                    registry.timer("my.timer")
                            .record(1, TimeUnit.MILLISECONDS);
                    registry.close();
                })
                .block(Duration.ofSeconds(10));

        assertFalse(receiveLatch.await(2, TimeUnit.SECONDS), "line was received");
        server.dispose();
        registry.close();
    }

    //TODO: fix
    @Disabled
    @Test
    void taggedMetrics() throws InterruptedException {
        final CountDownLatch receiveLatch = new CountDownLatch(1);

        BasicGraphiteMeterRegistry registry = new BasicGraphiteMeterRegistry(configuration, mockClock);

        Connection server = UdpServer.create()
                .option(ChannelOption.SO_REUSEADDR, true)
                .host(LOCALHOST)
                .port(configuration.getPort())
                .handle((in, out) -> {
                    in.receive()
                            .asString()
                            .subscribe(line -> {
                                //assertTrue(line.startsWith("my.timer;key=value;metricattribute=max "));
                                //System.out.println(line);
                                assertTrue(line.startsWith("my.timer.max 1.00"));
                                receiveLatch.countDown();
                            });
                    return Flux.never();
                })
                .bind()
                .doOnSuccess(v -> {
                    registry.timer("my.timer")
                            .record(1, TimeUnit.MILLISECONDS);
                    registry.close();
                })
                .block(Duration.ofSeconds(10));

        assertTrue(receiveLatch.await(10, TimeUnit.SECONDS), "line was received");
        server.dispose();
        registry.close();
    }

    //TODO: fix
    @Disabled
    @Test
    void alice() {
        BasicGraphiteMeterRegistry registry = new BasicGraphiteMeterRegistry(configuration, mockClock);
        UdpTestServer server = new UdpTestServer(configuration.getPort());
        server.runServerWithAssertion(line -> {
            System.out.println(line);
            assertTrue(line.startsWith("my"));
        }, v -> {
            registry.timer("my.timer").record(1, TimeUnit.MILLISECONDS);
            registry.close();
        });
    }

    public static int findAvailableUdpPort() {
        int min = 1024;
        Random random = new Random();
        int randomValue = (random.nextInt(65535) + min) - min;
        try {
            final DatagramSocket datagramSocket = new DatagramSocket(randomValue);
            assert (datagramSocket.isBound());
            datagramSocket.close();
            return randomValue;
        } catch (Exception ignored) {
        }
        throw new RuntimeException("no available UDP port");
    }
}