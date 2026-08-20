package dev.felicityiris.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.felicityiris.metrics.MetricCollector;
import dev.felicityiris.protocol.MetricUpdate;
import dev.felicityiris.protocol.SubscribeMessage;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSession {
    private final WebSocket connection;
    private final ObjectMapper objectMapper;

    private final Map<String, MetricCollector> collectors;

    private List<String> subscribedMetrics = List.of();
    private int interval = 1000;

    private Thread metricThread;
    private volatile boolean running;

    public ClientSession(
            WebSocket connection,
            ObjectMapper objectMapper,
            Map<String, MetricCollector> collectors
    ) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.collectors = collectors;
    }

    public void subscribe(SubscribeMessage message) {
        stop();

        subscribedMetrics = message.getMetrics();

        interval = Math.max(message.getInterval(), 100);

        running = true;

        metricThread = new Thread(this::metricLoop,
                "metrics-" + connection.getRemoteSocketAddress()
        );

        metricThread.start();
    }

    private void metricLoop() {
        while (running && connection.isOpen()) {
            Map<String, Double> data = new HashMap<>();

            for (String metricID : subscribedMetrics) {
                MetricCollector collector = collectors.get(metricID);

                if (collector == null) {
                    continue;
                }

                data.put(metricID, collector.collect());
            }

            if (!data.isEmpty()) {
                MetricUpdate update = new MetricUpdate(System.currentTimeMillis(), data);
                try {
                    connection.send(objectMapper.writeValueAsString(update));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;

        if (metricThread != null) {
            metricThread.interrupt();
            metricThread = null;
        }
    }
}
