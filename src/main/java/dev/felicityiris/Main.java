package dev.felicityiris;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.felicityiris.metrics.CpuCollector;
import dev.felicityiris.metrics.MetricCollector;
import dev.felicityiris.protocol.SubscribeMessage;
import dev.felicityiris.server.ClientSession;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static final int PORT = 8765;

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, MetricCollector> collectors = new HashMap<>();
        CpuCollector cpuCollector = new CpuCollector();
        collectors.put(cpuCollector.getMetricID(), cpuCollector);
        Map<WebSocket, ClientSession> sessions = new HashMap<>();

        WebSocketServer server = new WebSocketServer(new InetSocketAddress("0.0.0.0", PORT)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("Client connected: " + conn.getRemoteSocketAddress());

                ClientSession session = new ClientSession(conn, objectMapper, collectors);
                sessions.put(conn, session);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
                ClientSession session = sessions.remove(conn);

                if (session != null) {
                    session.stop();
                }
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                try {
                    SubscribeMessage subscribe = objectMapper.readValue(message, SubscribeMessage.class);
                    if (!"subscribe".equals(subscribe.getType())) {
                        return;
                    }

                    ClientSession session = sessions.get(conn);

                    if (session != null) {
                        session.subscribe(subscribe);
                    }
                } catch (Exception e) {
                    System.err.println("Invalid message: " + message);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                System.err.println("WebSocket error:");
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("RemoteSysMon Server started on port " + PORT);
            }
        };

        server.start();
    }
}