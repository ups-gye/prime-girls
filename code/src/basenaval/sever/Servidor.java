/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package basenaval.sever;

import basenaval.common.Protocolo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Servidor {

    // Mapa de salas activas
    private static Map<String, GameRoom> salas = new HashMap<>();
    private static int contadorSalas = 1;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9090);
            System.out.println("--- SERVIDOR MAESTRO ACTIVO (PUERTO 9090) ---");
            System.out.println(">>> Esperando conexiones... <<<");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream entrada;
        private DataOutputStream salida;
        public String usuario = "Desconocido"; 
        private GameRoom salaActual;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                entrada = new DataInputStream(socket.getInputStream());
                salida = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) { e.printStackTrace(); }
        }

        public void enviarMensaje(String msg) {
            try { salida.writeUTF(msg); } catch (IOException e) {}
        }

        private void manejarDesconexion() {
    try {
        if (salaActual != null) {
            salaActual.jugadorDesconectado(this);
        }
        System.out.println("Recursos liberados para " + usuario);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void cerrarConexion() {
    try {
        if (entrada != null) entrada.close();
        if (salida != null) salida.close();
        if (socket != null && !socket.isClosed()) socket.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

        @Override
        public void run() {
            try {
                // --- FASE 1: LEER EL PRIMER MENSAJE (LOGIN O REGISTER) ---
                String mensajeInicial = entrada.readUTF();
                GestorBD db = new GestorBD();

                // CASO A: REGISTRO
                if (mensajeInicial.startsWith(Protocolo.REGISTER)) {
                    System.out.println(">>> Solicitud de Registro...");
                    String[] partes = mensajeInicial.split(":");
                    
                    if (partes.length >= 6) {
                        boolean registrado = db.registrarUsuario(partes[1], partes[2], partes[3], partes[4], partes[5]);
                        if (registrado) {
                            enviarMensaje(Protocolo.REGISTER_OK);
                            System.out.println(">>> Nuevo usuario: " + partes[1]);
                        } else {
                            enviarMensaje(Protocolo.REGISTER_FAIL);
                        }
                    }
                    socket.close(); // Cerramos tras registro
                    return; 
                }
                
                // CASO B: LOGIN
                else if (mensajeInicial.startsWith(Protocolo.LOGIN)) {
                    String[] partes = mensajeInicial.split(":");
                    if (partes.length == 3 && db.validarUsuario(partes[1], partes[2])) {
                        this.usuario = partes[1];
                        enviarMensaje(Protocolo.LOGIN_OK);
                        System.out.println(">>> Login Correcto: " + this.usuario);
                    } else {
                        enviarMensaje(Protocolo.LOGIN_FAIL);
                        System.out.println(">>> Login Fallido: " + partes[1]);
                        socket.close(); 
                        return;
                    }
                } 
                else {
                    // Mensaje desconocido al inicio
                    socket.close(); 
                    return;
                }

                // --- FASE 2: BUCLE DEL JUEGO (SOLO LLEGA AQUÍ SI HUBO LOGIN) ---
                while (true) {
                    String mensaje = entrada.readUTF();

                    if (mensaje.startsWith(Protocolo.CREATE_ROOM)) {
                        String idSala = String.valueOf(contadorSalas++);
                        GameRoom nuevaSala = new GameRoom(idSala, this);
                        salas.put(idSala, nuevaSala);
                        this.salaActual = nuevaSala;
                        enviarMensaje(Protocolo.OPPONENT_TURN);
                        System.out.println("Sala #" + idSala + " creada por " + usuario);
                    }
                    else if (mensaje.equals(Protocolo.GET_ROOMS)) {
                        StringBuilder lista = new StringBuilder(Protocolo.LIST_ROOMS + ":");
                        for (GameRoom sala : salas.values()) {
                            if (!sala.estaLlena()) {
                                lista.append(sala.getId()).append("-").append(sala.getJugador1().usuario).append(";");
                            }
                        }
                        salida.writeUTF(lista.toString());
                    }
                    else if (mensaje.startsWith(Protocolo.JOIN_ROOM)) {
                        String idSala = mensaje.split(":")[1];
                        GameRoom sala = salas.get(idSala);
                        if (sala != null && !sala.estaLlena()) {
                            sala.unirJugador2(this);
                            this.salaActual = sala;
                            enviarMensaje(Protocolo.JOIN_OK);
                            System.out.println(usuario + " unido a Sala " + idSala);
                        } else { enviarMensaje("ERROR"); }
                    }
                    else if (mensaje.equals(Protocolo.GAME_READY)) {
                        if (salaActual != null) salaActual.setJugadorListo(this);
                    }
                    else if (mensaje.startsWith(Protocolo.SHOOT)) {
                        if (salaActual != null) {
                            String coords = mensaje.split(":")[1];
                            ClientHandler rival = salaActual.obtenerRival(this);
                            if (rival != null) rival.enviarMensaje(Protocolo.OPPONENT_SHOT + ":" + coords);
                        }
                    }
                    else if (mensaje.startsWith(Protocolo.RESULT_HIT) || mensaje.startsWith(Protocolo.RESULT_MISS)) {
                        if (salaActual != null) {
                            ClientHandler rival = salaActual.obtenerRival(this);
                            if (rival != null) rival.enviarMensaje(mensaje);
                        }
                    }
                }
            } catch (IOException e) {
    System.out.println("Usuario desconectado inesperadamente: " + usuario);
    manejarDesconexion();
} finally {
    cerrarConexion();
}

        }
    }
}