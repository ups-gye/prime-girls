package battleship.server;

import battleship.model.*;

import java.io.*;
import java.net.Socket;

/**
 * Hilo dedicado a gestionar la comunicación con un cliente.
 * Protocolo: "TIPO payload\n"  (espacio separa tipo del payload)
 * Sala usa ; como separador de campos.
 * Usuario usa , como separador de campos.
 * Entre salas en una lista: ~
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameServer server;
    private final RoomManager roomManager;
    private final UsuarioDAO dao;

    private PrintWriter out;
    private BufferedReader in;

    private Usuario usuario;
    private GameRoom currentRoom;
    private boolean isMonitor = false;
    private boolean running = true;

    public ClientHandler(Socket socket, GameServer server, RoomManager rm, UsuarioDAO dao) {
        this.socket = socket; this.server = server; this.roomManager = rm; this.dao = dao;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String line;
            while (running && (line = in.readLine()) != null) {
                if (!line.trim().isEmpty()) handleMessage(GameMessage.parse(line));
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Desconectado: " +
                (usuario != null ? usuario.getUsername() : socket.getRemoteSocketAddress()));
        } finally {
            handleDisconnect();
            closeQuietly();
        }
    }

    private void handleMessage(GameMessage msg) {
        System.out.println("[<-] " + (usuario != null ? usuario.getUsername() : "?") + " | " + msg.serialize());
        switch (msg.getType()) {
            case GameMessage.LOGIN:         handleLogin(msg);        break;
            case GameMessage.REGISTER:      handleRegister(msg);     break;
            case GameMessage.MONITOR_LOGIN: handleMonitorLogin(msg); break;
            case GameMessage.GET_ROOMS:     handleGetRooms();        break;
            case GameMessage.CREATE_ROOM:   handleCreateRoom();      break;
            case GameMessage.JOIN_ROOM:     handleJoinRoom(msg);     break;
            case GameMessage.PLACE_SHIPS:   handlePlaceShips(msg);   break;
            case GameMessage.SHOOT:         handleShoot(msg);        break;
            case GameMessage.LEAVE_ROOM:    handleLeaveRoom();       break;
            case GameMessage.GET_STATUS:    handleGetRooms();        break;
            default: send(new GameMessage(GameMessage.ERROR, "Comando desconocido"));
        }
    }

    // LOGIN username password
    private void handleLogin(GameMessage msg) {
        String username = msg.get(0);
        String password = msg.get(1);
        Usuario u = dao.findByCredentials(username, password);
        if (u == null) { send(new GameMessage(GameMessage.LOGIN_FAIL, "Usuario o contraseña incorrectos")); return; }
        this.usuario = u;
        // LOGIN_OK ganadas perdidas datos_usuario
        send(new GameMessage(GameMessage.LOGIN_OK,
            u.getPartidasGanadas() + " " + u.getPartidasPerdidas() + " " + u.toPublicString()));
        System.out.println("[SERVER] Login: " + username);
    }

    // REGISTER username password nombre apellido avatar
    private void handleRegister(GameMessage msg) {
        // payload: "username password nombre apellido avatar"
        String[] p = msg.getPayload().split(" ", 5);
        if (p.length < 4) { send(new GameMessage(GameMessage.REGISTER_FAIL, "Campos incompletos")); return; }
        String username = p[0], password = p[1], nombre = p[2], apellido = p[3];
        String avatar = p.length > 4 ? p[4] : "captain-1";
        if (dao.findByUsername(username) != null) {
            send(new GameMessage(GameMessage.REGISTER_FAIL, "El usuario ya existe")); return;
        }
        try {
            Usuario u = dao.create(new Usuario(username, password, nombre, apellido, avatar));
            send(new GameMessage(GameMessage.REGISTER_OK, u.toPublicString()));
        } catch (Exception e) {
            send(new GameMessage(GameMessage.REGISTER_FAIL, "Error al registrar: " + e.getMessage()));
        }
    }

    // MONITOR_LOGIN password
    private void handleMonitorLogin(GameMessage msg) {
        if ("admin123".equals(msg.getPayload().trim())) {
            isMonitor = true;
            send(new GameMessage(GameMessage.LOGIN_OK, "monitor"));
            server.addMonitor(this);
        } else {
            send(new GameMessage(GameMessage.LOGIN_FAIL, "Contrasena incorrecta"));
        }
    }

    private void handleGetRooms() {
        StringBuilder sb = new StringBuilder();
        for (GameRoom room : roomManager.getActiveRooms()) {
            if (sb.length() > 0) sb.append("~");
            sb.append(room.serialize());
        }
        send(new GameMessage(GameMessage.ROOM_LIST, sb.toString()));
    }

    private void handleCreateRoom() {
        if (usuario == null) { send(new GameMessage(GameMessage.ERROR, "No autenticado")); return; }
        GameRoom room = roomManager.createRoom(usuario, this);
        if (room == null) { send(new GameMessage(GameMessage.SERVER_FULL, "No se puede crear sala")); return; }
        currentRoom = room;
        send(new GameMessage(GameMessage.ROOM_CREATED, room.getId() + " " + room.getNombre()));
        server.broadcastRoomUpdate();
    }

    // JOIN_ROOM roomId
    private void handleJoinRoom(GameMessage msg) {
        if (usuario == null) { send(new GameMessage(GameMessage.ERROR, "No autenticado")); return; }
        int roomId;
        try { roomId = Integer.parseInt(msg.getPayload().trim()); }
        catch (NumberFormatException e) { send(new GameMessage(GameMessage.ERROR, "ID invalido")); return; }

        GameRoom room = roomManager.joinRoom(roomId, usuario, this);
        if (room == null) { send(new GameMessage(GameMessage.ROOM_FULL, "Sala llena o no disponible")); return; }
        currentRoom = room;

        // Notificar a J2 (yo)
        send(new GameMessage(GameMessage.ROOM_JOINED,
            room.getId() + " " + room.getNombre() + " " + room.getJugador1().getUsername()));
        // Notificar a J1
        ClientHandler h1 = roomManager.getHandler1(room.getId());
        if (h1 != null) h1.send(new GameMessage(GameMessage.ROOM_JOINED,
            room.getId() + " " + room.getNombre() + " " + usuario.getUsername()));
        server.broadcastRoomUpdate();
    }

    // PLACE_SHIPS serialized_data
    private void handlePlaceShips(GameMessage msg) {
        if (usuario == null || currentRoom == null) {
            send(new GameMessage(GameMessage.ERROR, "No estas en sala")); return;
        }
        Board board = Board.fromSerializedShips(msg.getPayload());
        boolean bothReady = roomManager.setBoard(currentRoom.getId(), usuario.getId(), board);
        send(new GameMessage(GameMessage.SHIPS_OK));

        if (bothReady) {
            GameRoom room = roomManager.getRoom(currentRoom.getId());
            ClientHandler h1 = roomManager.getHandler1(room.getId());
            ClientHandler h2 = roomManager.getHandler2(room.getId());
            if (h1 != null) h1.send(new GameMessage(GameMessage.GAME_START, room.getJugador2().getUsername()));
            if (h2 != null) h2.send(new GameMessage(GameMessage.GAME_START, room.getJugador1().getUsername()));
            if (h1 != null) h1.send(new GameMessage(GameMessage.YOUR_TURN));
            if (h2 != null) h2.send(new GameMessage(GameMessage.OPPONENT_TURN));
            server.broadcastRoomUpdate();
        }
    }

    // SHOOT fila columna
    private void handleShoot(GameMessage msg) {
        if (usuario == null || currentRoom == null) {
            send(new GameMessage(GameMessage.ERROR, "No estas en sala")); return;
        }
        int row, col;
        try {
            row = Integer.parseInt(msg.get(0));
            col = Integer.parseInt(msg.get(1));
        } catch (NumberFormatException e) {
            send(new GameMessage(GameMessage.ERROR, "Coordenadas invalidas")); return;
        }

        RoomManager.ShotResult result = roomManager.processShot(currentRoom.getId(), usuario.getId(), row, col);
        if (result.boardResult == Board.ShotResult.INVALID || result.boardResult == Board.ShotResult.ALREADY_SHOT) {
            send(new GameMessage(GameMessage.ERROR, "Disparo invalido")); return;
        }

        String tipo = result.boardResult == Board.ShotResult.MISS ? GameMessage.MISS
                    : result.boardResult == Board.ShotResult.SUNK  ? GameMessage.SUNK
                    : GameMessage.HIT;

        ClientHandler opp = roomManager.getOpponentHandler(currentRoom.getId(), usuario.getId());
        // SHOT_RESULT HIT|MISS|SUNK fila col
        send(new GameMessage(GameMessage.SHOT_RESULT, tipo + " " + row + " " + col));
        if (opp != null) opp.send(new GameMessage(GameMessage.OPPONENT_SHOT, tipo + " " + row + " " + col));

        if (result.winner) {
            send(new GameMessage(GameMessage.GAME_OVER, GameMessage.WIN));
            if (opp != null) opp.send(new GameMessage(GameMessage.GAME_OVER, GameMessage.LOSE));
            GameRoom room = roomManager.getRoom(currentRoom.getId());
            if (room != null) {
                Usuario loser = room.getOponente(usuario.getId());
                dao.updateStats(usuario.getId(), 1, 0, 100);
                if (loser != null) dao.updateStats(loser.getId(), 0, 1, 10);
            }
            roomManager.cleanRoom(currentRoom.getId());
            currentRoom = null;
            server.broadcastRoomUpdate();
        } else {
            send(new GameMessage(GameMessage.OPPONENT_TURN));
            if (opp != null) opp.send(new GameMessage(GameMessage.YOUR_TURN));
            server.broadcastRoomUpdate();
        }
    }

    private void handleLeaveRoom() {
        if (currentRoom == null) return;
        ClientHandler opp = roomManager.getOpponentHandler(currentRoom.getId(), usuario != null ? usuario.getId() : -1);
        roomManager.leaveRoom(currentRoom.getId(), usuario != null ? usuario.getId() : -1);
        if (opp != null) opp.send(new GameMessage(GameMessage.OPPONENT_LEFT));
        currentRoom = null;
        server.broadcastRoomUpdate();
    }

    private void handleDisconnect() {
        if (isMonitor) { server.removeMonitor(this); return; }
        if (currentRoom != null && usuario != null) {
            ClientHandler opp = roomManager.getOpponentHandler(currentRoom.getId(), usuario.getId());
            roomManager.leaveRoom(currentRoom.getId(), usuario.getId());
            if (opp != null) opp.send(new GameMessage(GameMessage.OPPONENT_LEFT));
            server.broadcastRoomUpdate();
        }
        running = false;
    }

    public void send(GameMessage msg) {
        if (out != null && !socket.isClosed()) {
            System.out.println("[->] " + (usuario != null ? usuario.getUsername() : "?") + " | " + msg.serialize());
            out.println(msg.serialize());
        }
    }

    private void closeQuietly() {
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }

    public Usuario getUsuario() { return usuario; }
    public boolean isMonitor() { return isMonitor; }
    public void stop() { running = false; closeQuietly(); }
}
