package dev.felicityiris;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static final int PORT = 8765;

    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServer(
                new InetSocketAddress("0.0.0.0", PORT)
        ) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println(
                        "Client connected: " + conn.getRemoteSocketAddress()
                );

                conn.send("""
                        {
                            "type": "test",
                            "message": "Hello from RemoteSysMon Server"
                        }
                        """);
            }

            @Override
            public void onClose(
                    WebSocket conn,
                    int code,
                    String reason,
                    boolean remote
            ) {
                System.out.println(
                        "Client disconnected: " + conn.getRemoteSocketAddress()
                );
            }

            @Override
            public void onMessage(WebSocket Conn, String message) {
                System.out.println("Received: " + message);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                System.err.println("WebSocket error:");
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println(
                        "RemoteSysMon Server started on port " + PORT
                );
            }
        };

        server.start();
    }
}