package battleship.model;

import java.io.Serializable;

/**
 * Protocolo TCP. Formato en cable: "TIPO payload"  (un espacio separa tipo de datos)
 * El payload puede contener cualquier caracter excepto salto de línea.
 * Esto evita colisiones con separadores internos de sala (;) y usuario (|).
 */
public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // ── Cliente → Servidor ───────────────────────────────────────────
    public static final String LOGIN         = "LOGIN";
    public static final String REGISTER      = "REGISTER";
    public static final String GET_ROOMS     = "GET_ROOMS";
    public static final String CREATE_ROOM   = "CREATE_ROOM";
    public static final String JOIN_ROOM     = "JOIN_ROOM";
    public static final String PLACE_SHIPS   = "PLACE_SHIPS";
    public static final String SHOOT         = "SHOOT";
    public static final String LEAVE_ROOM    = "LEAVE_ROOM";
    public static final String MONITOR_LOGIN = "MONITOR_LOGIN";
    public static final String GET_STATUS    = "GET_STATUS";

    // ── Servidor → Cliente ───────────────────────────────────────────
    public static final String LOGIN_OK       = "LOGIN_OK";
    public static final String LOGIN_FAIL     = "LOGIN_FAIL";
    public static final String REGISTER_OK    = "REGISTER_OK";
    public static final String REGISTER_FAIL  = "REGISTER_FAIL";
    public static final String ROOM_LIST      = "ROOM_LIST";
    public static final String ROOM_CREATED   = "ROOM_CREATED";
    public static final String ROOM_JOINED    = "ROOM_JOINED";
    public static final String ROOM_FULL      = "ROOM_FULL";
    public static final String ROOM_NOT_FOUND = "ROOM_NOT_FOUND";
    public static final String SERVER_FULL    = "SERVER_FULL";
    public static final String SHIPS_OK       = "SHIPS_OK";
    public static final String GAME_START     = "GAME_START";
    public static final String YOUR_TURN      = "YOUR_TURN";
    public static final String OPPONENT_TURN  = "OPPONENT_TURN";
    public static final String SHOT_RESULT    = "SHOT_RESULT";
    public static final String OPPONENT_SHOT  = "OPPONENT_SHOT";
    public static final String SHIP_SUNK      = "SHIP_SUNK";
    public static final String GAME_OVER      = "GAME_OVER";
    public static final String OPPONENT_LEFT  = "OPPONENT_LEFT";
    public static final String ERROR          = "ERROR";
    public static final String STATUS_UPDATE  = "STATUS_UPDATE";

    // ── Resultados ───────────────────────────────────────────────────
    public static final String HIT  = "HIT";
    public static final String MISS = "MISS";
    public static final String SUNK = "SUNK";
    public static final String WIN  = "WIN";
    public static final String LOSE = "LOSE";

    private final String type;
    private final String payload; // el string COMPLETO después del primer espacio

    public GameMessage(String type, String payload) {
        this.type = type;
        this.payload = payload != null ? payload : "";
    }

    /** Constructor de conveniencia: une los args con espacio como payload */
    public GameMessage(String type, String... args) {
        this.type = type;
        this.payload = args == null || args.length == 0 ? "" : String.join(" ", args);
    }

    /** Serializa a línea de texto: "TIPO payload\n" */
    public String serialize() {
        if (payload.isEmpty()) return type;
        return type + " " + payload;
    }

    /** Parsea desde una línea de texto */
    public static GameMessage parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new GameMessage(ERROR, "");
        int idx = raw.indexOf(' ');
        if (idx < 0) return new GameMessage(raw.trim(), "");
        return new GameMessage(raw.substring(0, idx), raw.substring(idx + 1));
    }

    /** Devuelve el payload completo (para ROOM_LIST, PLACE_SHIPS, etc.) */
    public String getPayload() { return payload; }

    /**
     * Devuelve el campo N del payload cuando este está separado por espacios.
     * Para mensajes simples como "SHOOT 3 5" → get(0)="3", get(1)="5"
     * Para mensajes de payload complejo usa getPayload() directamente.
     */
    public String get(int index) {
        if (payload.isEmpty()) return "";
        String[] parts = payload.split(" ");
        if (index >= parts.length) return "";
        return parts[index];
    }

    public String getType() { return type; }

    @Override
    public String toString() { return serialize(); }
}
