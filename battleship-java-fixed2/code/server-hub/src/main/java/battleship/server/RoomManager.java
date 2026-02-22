package battleship.server;

import battleship.model.GameRoom;
import battleship.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de salas. Máximo 4 salas activas simultáneas.
 * Thread-safe usando synchronized.
 */
public class RoomManager {

    private static final int MAX_ROOMS = 4;

    // roomId → GameRoom
    private final ConcurrentHashMap<Integer, GameRoom> rooms = new ConcurrentHashMap<>();
    // roomId → handler del jugador 1
    private final ConcurrentHashMap<Integer, ClientHandler> handlers1 = new ConcurrentHashMap<>();
    // roomId → handler del jugador 2
    private final ConcurrentHashMap<Integer, ClientHandler> handlers2 = new ConcurrentHashMap<>();
    // roomId → tablero del jugador 1
    private final ConcurrentHashMap<Integer, battleship.model.Board> boards1 = new ConcurrentHashMap<>();
    // roomId → tablero del jugador 2
    private final ConcurrentHashMap<Integer, battleship.model.Board> boards2 = new ConcurrentHashMap<>();

    private int nextId = 1;

    /** Crea una sala nueva para jugador1. Retorna null si hay 4 salas activas. */
    public synchronized GameRoom createRoom(Usuario jugador1, ClientHandler handler) {
        long active = rooms.values().stream()
                .filter(r -> r.getEstado() != GameRoom.Estado.FINALIZADA)
                .count();
        if (active >= MAX_ROOMS) return null;

        // Verificar que el jugador no esté en otra sala activa
        if (findRoomByUser(jugador1.getId()) != null) return null;

        GameRoom room = new GameRoom(nextId++, jugador1);
        rooms.put(room.getId(), room);
        handlers1.put(room.getId(), handler);
        return room;
    }

    /** Jugador2 se une a la sala. */
    public synchronized GameRoom joinRoom(int roomId, Usuario jugador2, ClientHandler handler) {
        GameRoom room = rooms.get(roomId);
        if (room == null) return null;
        if (room.getEstado() != GameRoom.Estado.ESPERANDO) return null;
        if (room.getJugador1().getId() == jugador2.getId()) return null;
        if (findRoomByUser(jugador2.getId()) != null) return null;

        room.setJugador2(jugador2);
        handlers2.put(roomId, handler);
        return room;
    }

    /** Registra el tablero de un jugador. Retorna true si ambos ya colocaron. */
    public synchronized boolean setBoard(int roomId, int userId, battleship.model.Board board) {
        GameRoom room = rooms.get(roomId);
        if (room == null) return false;

        if (room.getJugador1() != null && room.getJugador1().getId() == userId) {
            boards1.put(roomId, board);
        } else if (room.getJugador2() != null && room.getJugador2().getId() == userId) {
            boards2.put(roomId, board);
        } else {
            return false;
        }

        boolean ambosListos = boards1.containsKey(roomId) && boards2.containsKey(roomId);
        if (ambosListos && room.getEstado() == GameRoom.Estado.CONFIGURANDO) {
            room.iniciarJuego();
        }
        return ambosListos;
    }

    /** Procesa un disparo. Retorna array: [resultado, hundido, ganador] */
    public synchronized ShotResult processShot(int roomId, int shooterId, int row, int col) {
        GameRoom room = rooms.get(roomId);
        if (room == null || room.getEstado() != GameRoom.Estado.EN_JUEGO) {
            return new ShotResult(battleship.model.Board.ShotResult.INVALID, false, false);
        }
        if (room.getTurnoActualId() != shooterId) {
            return new ShotResult(battleship.model.Board.ShotResult.INVALID, false, false);
        }

        // Disparar al tablero del oponente
        boolean esJ1 = room.getJugador1().getId() == shooterId;
        battleship.model.Board targetBoard = esJ1 ? boards2.get(roomId) : boards1.get(roomId);
        if (targetBoard == null) return new ShotResult(battleship.model.Board.ShotResult.INVALID, false, false);

        battleship.model.Board.ShotResult result = targetBoard.shoot(row, col);
        boolean sunk = result == battleship.model.Board.ShotResult.SUNK;
        boolean winner = targetBoard.allSunk();

        room.registrarDisparo(shooterId, result != battleship.model.Board.ShotResult.MISS, sunk);

        if (winner) {
            room.finalizar();
        } else {
            room.cambiarTurno();
        }

        return new ShotResult(result, sunk, winner);
    }

    /** Un jugador sale de la sala */
    public synchronized void leaveRoom(int roomId, int userId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) return;
        if (room.getEstado() != GameRoom.Estado.FINALIZADA) {
            room.finalizar();
        }
    }

    /** Busca la sala activa donde está un jugador */
    public GameRoom findRoomByUser(int userId) {
        for (GameRoom room : rooms.values()) {
            if (room.getEstado() != GameRoom.Estado.FINALIZADA && room.tieneJugador(userId)) {
                return room;
            }
        }
        return null;
    }

    public GameRoom getRoom(int roomId) { return rooms.get(roomId); }

    public ClientHandler getHandler1(int roomId) { return handlers1.get(roomId); }
    public ClientHandler getHandler2(int roomId) { return handlers2.get(roomId); }

    public ClientHandler getOpponentHandler(int roomId, int userId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) return null;
        if (room.getJugador1() != null && room.getJugador1().getId() == userId)
            return handlers2.get(roomId);
        return handlers1.get(roomId);
    }

    /** Lista todas las salas activas (para el monitor) */
    public List<GameRoom> getActiveRooms() {
        List<GameRoom> list = new ArrayList<>();
        for (GameRoom r : rooms.values()) {
            if (r.getEstado() != GameRoom.Estado.FINALIZADA) list.add(r);
        }
        return list;
    }

    /** Limpia una sala finalizada de los mapas */
    public synchronized void cleanRoom(int roomId) {
        boards1.remove(roomId);
        boards2.remove(roomId);
        handlers1.remove(roomId);
        handlers2.remove(roomId);
    }

    /** Resultado de un disparo */
    public static class ShotResult {
        public final battleship.model.Board.ShotResult boardResult;
        public final boolean sunk;
        public final boolean winner;

        public ShotResult(battleship.model.Board.ShotResult r, boolean sunk, boolean winner) {
            this.boardResult = r;
            this.sunk = sunk;
            this.winner = winner;
        }
    }
}
