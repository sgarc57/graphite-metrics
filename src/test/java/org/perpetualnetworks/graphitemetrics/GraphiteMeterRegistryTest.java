package org.perpetualnetworks.graphitemetrics;

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Timer;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.net.DatagramSocket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class GraphiteMeterRegistryTest {

    public static final String LOCALHOST = "127.0.0.1";
    public static final int LATCH_ITERATIONS = 20;
    private final MockClock mockClock = new MockClock();

    GraphiteConfiguration configuration = GraphiteConfiguration.builder()
            //.port(findAvailableUdpPort())
            //to view / capture traffic: $ tshark -z follow,udp,ascii,1 -i any udp port 1024
            .port(1024)
            .prefix("bob")
            .enabled(true)
            .tagsEnabled(true)
            .tags(List.of("bob", "alice"))
            .senderName("sendername")
            .rateUnits(TimeUnit.MILLISECONDS)
            .durationUnits(TimeUnit.SECONDS)
            .step(Duration.ofSeconds(1))
            .stepUnits(ChronoUnit.SECONDS)
            .host(LOCALHOST)
            .executorService(new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    final Thread thread = new Thread(runnable);
                    return thread;
                }
            }))
            .shutdownExecutorOnStop(false)
            .build();

    @Test
    synchronized void metricPrefixes() throws InterruptedException {

        final CountDownLatch receiveLatch = new CountDownLatch(LATCH_ITERATIONS);
        //registy automtically starts from construction
        GraphiteMeterRegistry registry = new GraphiteMeterRegistry(configuration, mockClock);
        final String timerName = "my.timer";
        final String[] timerTags = {"tag1", "tag2"};
        final Timer timer = registry.timer(timerName, timerTags);
        Connection server = UdpServer.create()
                .option(ChannelOption.SO_REUSEADDR, true)
                .host(configuration.getHost())
                .port(configuration.getPort())
                .handle((in, out) -> {
                    in.receive()
                            .asString()
                            .subscribe(line -> {
                                assertTrue(line.startsWith(configuration.getPrefix()));
                                receiveLatch.countDown();
                            });
                    return Flux.never();
                })
                .bind()
                .doOnSuccess(v -> {
                    timer.record(1, TimeUnit.SECONDS);
                })
                .block(Duration.ofSeconds(10));

        assert server != null;
        try {
            log.info("latch count: " + receiveLatch.getCount());
            assertFalse(receiveLatch.await(60, TimeUnit.MILLISECONDS), "line was received");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(timer.count());
        registry.stop();
        registry.close();
    }

    @Test
    void taggedMetrics() throws InterruptedException {
        final CountDownLatch receiveLatch = new CountDownLatch(LATCH_ITERATIONS);

        GraphiteMeterRegistry registry = new GraphiteMeterRegistry(configuration, mockClock);

        final String timerName = "my.timer";
        final String[] timerTags = {"tag1", "tag2"};
        final Timer timer = registry.timer(timerName, timerTags);

        Connection server = UdpServer.create()
                .option(ChannelOption.SO_REUSEADDR, true)
                .host(configuration.getHost())
                .port(configuration.getPort())
                .handle((in, out) -> {
                    in.receive()
                            .asString()
                            .subscribe(line -> {
                                System.out.println(line);
                                assertTrue(line.startsWith(configuration.getPrefix() + "." + timerName + "." + String.join(".", timerTags)));
                                receiveLatch.countDown();
                            });
                    return Flux.never();
                })
                .bind()
                .doOnSuccess(v -> {
                    timer.record(1, TimeUnit.MILLISECONDS);
                    registry.stop();
                })
                .block(Duration.ofSeconds(10));

        assertFalse(receiveLatch.await(60, TimeUnit.MILLISECONDS), "line was received");
        assert server != null;
        server.dispose();
        registry.close();
    }

    @Test
    void simpleServerTest() {
        GraphiteMeterRegistry registry = new GraphiteMeterRegistry(configuration, mockClock);
        UdpTestServer server = new UdpTestServer(configuration.getPort());
        server.runServerWithAssertion(line -> {
            System.out.println(line);
            assertTrue(line.startsWith(configuration.getPrefix() + "." + "my.timer"));
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