package basenaval.sever;

import basenaval.common.Protocolo;

public class GameRoom {
    private String id;
    private Servidor.ClientHandler jugador1;
    private Servidor.ClientHandler jugador2;
    
    private boolean j1Listo = false;
    private boolean j2Listo = false;

    public GameRoom(String id, Servidor.ClientHandler j1) {
        this.id = id;
        this.jugador1 = j1;
    }

    public String getId() {
        return id;
    }

    public Servidor.ClientHandler getJugador1() {
        return jugador1;
    }
    public void unirJugador2(Servidor.ClientHandler j2) {
        this.jugador2 = j2;
    }

    public boolean estaLlena() {
        return jugador2 != null;
    }
    public Servidor.ClientHandler obtenerRival(Servidor.ClientHandler actual) {
        if (actual == jugador1) return jugador2;
        if (actual == jugador2) return jugador1;
        return null; 
    }
    public void setJugadorListo(Servidor.ClientHandler j) {
        if (j == jugador1) j1Listo = true;
        if (j == jugador2) j2Listo = true;

        if (j1Listo && j2Listo) {
            iniciarJuego();
        }
    }

    private void iniciarJuego() {
        try {
            if (jugador1 != null) jugador1.enviarMensaje(Protocolo.GAME_START);
            if (jugador2 != null) jugador2.enviarMensaje(Protocolo.GAME_START);
            System.out.println("Sala " + id + ": ¡Juego iniciado!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}