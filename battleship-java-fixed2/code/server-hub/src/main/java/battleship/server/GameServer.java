package battleship.server;

import battleship.model.GameMessage;
import battleship.model.GameRoom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    public static final int PORT = 9090;

    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(50);
    private final RoomManager roomManager = new RoomManager();
    private final UsuarioDAO dao = new UsuarioDAO();
    private boolean running = false;

    private final List<ClientHandler> monitors = new ArrayList<>();

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     BATALLA NAVAL - SERVIDOR HUB     ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  Puerto : " + PORT + "                         ║");
        System.out.println("║  Protocolo: TIPO payload (espacio)   ║");
        System.out.println("║  Estado : EN LÍNEA                   ║");
        System.out.println("╚══════════════════════════════════════╝");

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        while (running) {
            try {
                Socket s = serverSocket.accept();
                System.out.println("[SERVER] Conexión: " + s.getRemoteSocketAddress());
                threadPool.execute(new ClientHandler(s, this, roomManager, dao));
            } catch (IOException e) {
                if (running) System.err.println("[SERVER] Error aceptando: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        threadPool.shutdownNow();
        UsuarioDAO.shutdown();
        System.out.println("[SERVER] Apagado.");
    }

    public synchronized void addMonitor(ClientHandler m) {
        monitors.add(m);
        System.out.println("[SERVER] Monitor conectado.");
    }

    public synchronized void removeMonitor(ClientHandler m) { monitors.remove(m); }

    public synchronized void broadcastRoomUpdate() {
        if (monitors.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (GameRoom r : roomManager.getActiveRooms()) {
            if (sb.length() > 0) sb.append("~");
            sb.append(r.serialize());
        }
        GameMessage upd = new GameMessage(GameMessage.STATUS_UPDATE, sb.toString());
        List<ClientHandler> dead = new ArrayList<>();
        for (ClientHandler m : monitors) {
            try { m.send(upd); }
            catch (Exception e) { dead.add(m); }
        }
        monitors.removeAll(dead);
    }

    public RoomManager getRoomManager() { return roomManager; }
    public UsuarioDAO getDao() { return dao; }

    public static void main(String[] args) {
        try { new GameServer().start(); }
        catch (IOException e) { System.err.println("[SERVER] Fatal: " + e.getMessage()); System.exit(1); }
    }
}
