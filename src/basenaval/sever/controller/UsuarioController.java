package basenaval.sever.controller;

import basenaval.sever.GestorBD;
import basenaval.sever.model.Usuario;

/**
 * Controlador MVC para operaciones sobre usuarios.
 * Actúa como puente entre el Servidor y la capa de persistencia (GestorBD).
 * Los clientes NUNCA acceden a GestorBD directamente.
 */
public class UsuarioController {

    private final GestorBD db = new GestorBD();

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    public boolean registrar(Usuario u) {
        if (u == null) return false;
        return db.registrarUsuario(
            u.getUsername(),
            u.getPassword(),
            u.getNombre(),
            u.getApellido(),
            u.getAvatar()
        );
    }

    // ─────────────────────────────────────────────
    //  READ — Login
    // ─────────────────────────────────────────────
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        return db.validarUsuario(username, password);
    }

    // ─────────────────────────────────────────────
    //  READ — Perfil
    // ─────────────────────────────────────────────
    public Usuario obtenerPerfil(String username) {
        if (username == null) return null;
        return db.obtenerUsuario(username);
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    public boolean actualizarPassword(String username, String nuevaPassword) {
        if (username == null || nuevaPassword == null || nuevaPassword.isBlank()) return false;
        return db.actualizarPassword(username, nuevaPassword);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    public boolean eliminarUsuario(String username) {
        if (username == null) return false;
        return db.eliminarUsuario(username);
    }
}