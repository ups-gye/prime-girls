package basenaval.common;


/**
 * Protocolo de comunicación Cliente ↔ Servidor.
 * Formato general: COMANDO:param1:param2:...
 */
public class Protocolo {

    // ── Autenticación ─────────────────────────────
    public static final String LOGIN        = "LOGIN";
    public static final String LOGIN_OK     = "OK_LOGIN";
    public static final String LOGIN_FAIL   = "NO_LOGIN";

    public static final String REGISTER     = "REGISTER";
    public static final String REGISTER_OK  = "REG_OK";
    public static final String REGISTER_FAIL = "REG_FAIL";

    // ── CRUD Usuario ──────────────────────────────
    public static final String GET_PROFILE   = "GET_PROFILE";
    public static final String PROFILE_DATA  = "PROFILE_DATA";  // respuesta: PROFILE_DATA:nombre:apellido:avatar

    public static final String UPDATE_PASS   = "UPDATE_PASS";   // UPDATE_PASS:nuevaPassword
    public static final String UPDATE_OK     = "UPDATE_OK";

    public static final String DELETE_USER   = "DELETE_USER";
    public static final String DELETE_OK     = "DELETE_OK";

    // ── Lobby / Salas ─────────────────────────────
    public static final String CREATE_ROOM  = "CREATE";
    public static final String GET_ROOMS    = "GET_ROOMS";
    public static final String LIST_ROOMS   = "LISTA";
    public static final String JOIN_ROOM    = "JOIN";
    public static final String JOIN_OK      = "JOIN_OK";
    public static final String OPPONENT_TURN = "ESPERA";

    // ── Juego ─────────────────────────────────────
    public static final String GAME_READY   = "READY";
    public static final String GAME_START   = "START";
    public static final String SHOOT        = "SHOOT";
    public static final String OPPONENT_SHOT = "OPP_SHOT";
    public static final String RESULT_HIT   = "HIT";
    public static final String RESULT_MISS  = "MISS";
}