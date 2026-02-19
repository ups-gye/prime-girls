package basenaval.sever;

import basenaval.common.Protocolo;
import basenaval.sever.controller.UsuarioController;
import basenaval.sever.model.Usuario;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {

    private static Map<String, GameRoom> salas = new HashMap<>();
    private static int contadorSalas = 1;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║  BATTLESHIP SERVER  — Puerto 9090    ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.println(">>> Esperando conexiones...");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════
    //  HILO POR CLIENTE
    // ══════════════════════════════════════════════
    public static class ClientHandler extends Thread {

        private Socket socket;
        private DataInputStream  entrada;
        private DataOutputStream salida;
        public  String usuario = "Desconocido";
        private GameRoom salaActual;

        // Un único controlador por hilo (no por mensaje)
        private final UsuarioController ctrl = new UsuarioController();

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                entrada = new DataInputStream(socket.getInputStream());
                salida  = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) { e.printStackTrace(); }
        }

        public void enviarMensaje(String msg) {
            try { salida.writeUTF(msg); } catch (IOException e) {}
        }

        @Override
        public void run() {
            try {

                // ── FASE 1: LOGIN o REGISTER ───────────────────
                String mensajeInicial = entrada.readUTF();

                if (mensajeInicial.startsWith(Protocolo.REGISTER)) {
                    // Formato: REGISTER:username:password:nombre:apellido:avatar
                    String[] p = mensajeInicial.split(":");
                    if (p.length >= 6) {
                        Usuario u = new Usuario(p[1], p[2], p[3], p[4], p[5]);
                        boolean ok = ctrl.registrar(u);
                        enviarMensaje(ok ? Protocolo.REGISTER_OK : Protocolo.REGISTER_FAIL);
                        System.out.println("[REGISTER] " + (ok ? "OK → " + p[1] : "FAIL → " + p[1]));
                    } else {
                        enviarMensaje(Protocolo.REGISTER_FAIL);
                    }
                    socket.close();
                    return;
                }

                else if (mensajeInicial.startsWith(Protocolo.LOGIN)) {
                    // Formato: LOGIN:username:password
                    String[] p = mensajeInicial.split(":");
                    if (p.length == 3 && ctrl.login(p[1], p[2])) {
                        this.usuario = p[1];
                        enviarMensaje(Protocolo.LOGIN_OK);
                        System.out.println("[LOGIN] OK → " + this.usuario);
                    } else {
                        enviarMensaje(Protocolo.LOGIN_FAIL);
                        System.out.println("[LOGIN] FAIL → " + (p.length > 1 ? p[1] : "?"));
                        socket.close();
                        return;
                    }
                }

                else {
                    socket.close();
                    return;
                }

                // ── FASE 2: BUCLE PRINCIPAL ────────────────────
                while (true) {
                    String mensaje = entrada.readUTF();

                    // ── CRUD: READ perfil ──────────────────────
                    if (mensaje.equals(Protocolo.GET_PROFILE)) {
                        Usuario u = ctrl.obtenerPerfil(this.usuario);
                        if (u != null) {
                            enviarMensaje(Protocolo.PROFILE_DATA + ":"
                                + u.getNombre() + ":" + u.getApellido() + ":" + u.getAvatar());
                        } else {
                            enviarMensaje("ERROR");
                        }
                    }

                    // ── CRUD: UPDATE contraseña ────────────────
                    else if (mensaje.startsWith(Protocolo.UPDATE_PASS)) {
                        String[] p = mensaje.split(":");
                        if (p.length >= 2 && !p[1].isBlank()) {
                            boolean ok = ctrl.actualizarPassword(this.usuario, p[1]);
                            enviarMensaje(ok ? Protocolo.UPDATE_OK : "ERROR");
                            System.out.println("[UPDATE_PASS] " + this.usuario + " → " + (ok ? "OK" : "FAIL"));
                        } else {
                            enviarMensaje("ERROR");
                        }
                    }

                    // ── CRUD: DELETE usuario ───────────────────
                    else if (mensaje.equals(Protocolo.DELETE_USER)) {
                        boolean ok = ctrl.eliminarUsuario(this.usuario);
                        enviarMensaje(ok ? Protocolo.DELETE_OK : "ERROR");
                        System.out.println("[DELETE] " + this.usuario + " → " + (ok ? "OK" : "FAIL"));
                        socket.close();
                        return;
                    }

                    // ── Lobby ──────────────────────────────────
                    else if (mensaje.startsWith(Protocolo.CREATE_ROOM)) {
                        String idSala = String.valueOf(contadorSalas++);
                        GameRoom nuevaSala = new GameRoom(idSala, this);
                        salas.put(idSala, nuevaSala);
                        this.salaActual = nuevaSala;
                        enviarMensaje(Protocolo.OPPONENT_TURN);
                        System.out.println("[SALA] #" + idSala + " creada por " + usuario);
                    }

                    else if (mensaje.equals(Protocolo.GET_ROOMS)) {
                        StringBuilder lista = new StringBuilder(Protocolo.LIST_ROOMS + ":");
                        for (GameRoom sala : salas.values()) {
                            if (!sala.estaLlena()) {
                                lista.append(sala.getId()).append("-")
                                     .append(sala.getJugador1().usuario).append(";");
                            }
                        }
                        enviarMensaje(lista.toString());
                    }

                    else if (mensaje.startsWith(Protocolo.JOIN_ROOM)) {
                        String idSala = mensaje.split(":")[1];
                        GameRoom sala = salas.get(idSala);
                        if (sala != null && !sala.estaLlena()) {
                            sala.unirJugador2(this);
                            this.salaActual = sala;
                            enviarMensaje(Protocolo.JOIN_OK);
                            System.out.println("[JOIN] " + usuario + " → Sala " + idSala);
                        } else {
                            enviarMensaje("ERROR");
                        }
                    }

                    else if (mensaje.equals(Protocolo.GAME_READY)) {
                        if (salaActual != null) salaActual.setJugadorListo(this);
                    }

                    // ── Juego ──────────────────────────────────
                    else if (mensaje.startsWith(Protocolo.SHOOT)) {
                        if (salaActual != null) {
                            String coords = mensaje.split(":")[1];
                            ClientHandler rival = salaActual.obtenerRival(this);
                            if (rival != null) rival.enviarMensaje(Protocolo.OPPONENT_SHOT + ":" + coords);
                        }
                    }

                    else if (mensaje.startsWith(Protocolo.RESULT_HIT)
                          || mensaje.startsWith(Protocolo.RESULT_MISS)) {
                        if (salaActual != null) {
                            ClientHandler rival = salaActual.obtenerRival(this);
                            if (rival != null) rival.enviarMensaje(mensaje);
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("[DESCONEXIÓN] " + usuario);
            }
        }
    }
}