package battleship.client;

import battleship.model.GameMessage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gestiona la conexión TCP del cliente con el servidor.
 * Usa un hilo dedicado para recibir mensajes y notifica a los listeners.
 */
public class NetworkClient {

    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private Thread listenerThread;

    private final CopyOnWriteArrayList<MessageListener> listeners = new CopyOnWriteArrayList<>();

    public interface MessageListener {
        void onMessage(GameMessage msg);
        void onDisconnect();
    }

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** Conecta al servidor. Retorna true si exitoso. */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            connected = true;

            // Hilo receptor
            listenerThread = new Thread(this::receiveLoop, "ClientReceiver");
            listenerThread.setDaemon(true);
            listenerThread.start();

            System.out.println("[CLIENT] Conectado a " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("[CLIENT] No se pudo conectar: " + e.getMessage());
            return false;
        }
    }

    /** Bucle de recepción en hilo separado */
    private void receiveLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                GameMessage msg = GameMessage.parse(line);
                System.out.println("[CLIENT] <- " + msg.serialize());
                for (MessageListener l : listeners) {
                    try { l.onMessage(msg); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        } catch (IOException e) {
            if (connected) System.out.println("[CLIENT] Conexión cerrada por el servidor.");
        } finally {
            connected = false;
            for (MessageListener l : listeners) {
                try { l.onDisconnect(); } catch (Exception ignored) {}
            }
        }
    }

    /** Envía un mensaje al servidor */
    public void send(GameMessage msg) {
        if (out != null && connected) {
            System.out.println("[CLIENT] -> " + msg.serialize());
            out.println(msg.serialize());
        }
    }

    public void addListener(MessageListener listener) { listeners.add(listener); }
    public void removeListener(MessageListener listener) { listeners.remove(listener); }

    public void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    public boolean isConnected() { return connected; }
    public String getHost() { return host; }
    public int getPort() { return port; }
}
