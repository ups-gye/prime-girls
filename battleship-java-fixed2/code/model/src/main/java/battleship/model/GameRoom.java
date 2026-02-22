package battleship.model;

import java.io.Serializable;

/**
 * Estado de una sala de juego.
 * SEPARADORES (nunca se mezclan):
 *   Protocolo:  espacio  (tipo payload)
 *   Entre salas: ~
 *   Campos de sala: ;
 *   Campos de usuario: ,
 */
public class GameRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Estado { ESPERANDO, CONFIGURANDO, EN_JUEGO, FINALIZADA }

    private int id;
    private String nombre;
    private Estado estado;
    private Usuario jugador1;
    private Usuario jugador2;
    private int turnoActualId;

    private int disparosJ1, aciertosJ1, hundidosJ1;
    private int disparosJ2, aciertosJ2, hundidosJ2;

    public GameRoom(int id, Usuario jugador1) {
        this.id = id;
        this.nombre = "Sala_" + id;   // sin espacios para que no rompa el split de payload
        this.jugador1 = jugador1;
        this.estado = Estado.ESPERANDO;
    }

    public void setJugador2(Usuario j2) {
        this.jugador2 = j2;
        this.estado = Estado.CONFIGURANDO;
    }

    public void iniciarJuego() {
        this.estado = Estado.EN_JUEGO;
        this.turnoActualId = jugador1.getId();
    }

    public void registrarDisparo(int jugadorId, boolean acierto, boolean hundido) {
        if (jugadorId == jugador1.getId()) {
            disparosJ1++; if (acierto) aciertosJ1++; if (hundido) hundidosJ1++;
        } else {
            disparosJ2++; if (acierto) aciertosJ2++; if (hundido) hundidosJ2++;
        }
    }

    public void cambiarTurno() {
        if (jugador1 != null && jugador2 != null)
            turnoActualId = (turnoActualId == jugador1.getId()) ? jugador2.getId() : jugador1.getId();
    }

    public void finalizar() { this.estado = Estado.FINALIZADA; }

    public boolean tieneJugador(int userId) {
        return (jugador1 != null && jugador1.getId() == userId)
            || (jugador2 != null && jugador2.getId() == userId);
    }

    public Usuario getOponente(int userId) {
        if (jugador1 != null && jugador1.getId() == userId) return jugador2;
        if (jugador2 != null && jugador2.getId() == userId) return jugador1;
        return null;
    }

    /**
     * Serializa la sala.
     * Formato: id;nombre;estado;j1_csv;j2_csv;turnoId;d1;a1;h1;d2;a2;h2
     * donde j1_csv y j2_csv usan coma internamente (Usuario.toPublicString)
     * y los campos de sala usan punto-y-coma.
     */
    public String serialize() {
        String j1s = jugador1 != null ? jugador1.toPublicString() : "null";
        String j2s = jugador2 != null ? jugador2.toPublicString() : "null";
        return id + ";" + nombre + ";" + estado.name() + ";"
                + j1s + ";" + j2s + ";"
                + turnoActualId + ";" + disparosJ1 + ";" + aciertosJ1 + ";" + hundidosJ1
                + ";" + disparosJ2 + ";" + aciertosJ2 + ";" + hundidosJ2;
    }

    // Getters & setters
    public int getId() { return id; }
    public String getNombre() { return nombre.replace("_", " "); }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado e) { this.estado = e; }
    public Usuario getJugador1() { return jugador1; }
    public Usuario getJugador2() { return jugador2; }
    public int getTurnoActualId() { return turnoActualId; }
    public void setTurnoActualId(int id) { this.turnoActualId = id; }
    public int getDisparosJ1() { return disparosJ1; }
    public int getAciertosJ1() { return aciertosJ1; }
    public int getHundidosJ1() { return hundidosJ1; }
    public int getDisparosJ2() { return disparosJ2; }
    public int getAciertosJ2() { return aciertosJ2; }
    public int getHundidosJ2() { return hundidosJ2; }

    @Override
    public String toString() {
        return getNombre() + " [" + estado + "] "
            + (jugador1 != null ? jugador1.getUsername() : "?")
            + " vs " + (jugador2 != null ? jugador2.getUsername() : "?");
    }
}
