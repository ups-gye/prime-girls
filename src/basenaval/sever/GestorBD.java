package basenaval.sever; // Asegúrate que coincida con tu paquete (ej. .sever o .server)

import basenaval.sever.model.Usuario;
import java.sql.*;

public class GestorBD {

    private static final String URL  = "jdbc:postgresql://localhost:5432/battleship_db";
    private static final String USER = "postgres";
    private static final String PASS = "F272jos46";

    // ─────────────────────────────────────────────
    //  CONEXIÓN
    // ─────────────────────────────────────────────
    public Connection conectar() {
    try {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    } catch (ClassNotFoundException e) {
        System.out.println("[BD] Driver no encontrado: " + e.getMessage());
        return null;
    } catch (SQLException e) {
        System.out.println("[BD] Error de conexión: " + e.getMessage());
        return null;
    }
}

    // ─────────────────────────────────────────────
    //  CREATE — Registrar usuario
    // ─────────────────────────────────────────────
    public boolean registrarUsuario(String username, String password,
                                    String nombre, String apellido, String avatar) {
        String sql = "INSERT INTO usuarios (username, password, nombre, apellido, avatar) VALUES (?,?,?,?,?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, nombre);
            ps.setString(4, apellido);
            ps.setString(5, avatar);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[BD] Error al registrar: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────
    //  READ — Validar login
    // ─────────────────────────────────────────────
    public boolean validarUsuario(String username, String password) {
        String sql = "SELECT id FROM usuarios WHERE username = ? AND password = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────
    //  READ — Obtener perfil completo
    // ─────────────────────────────────────────────
    public Usuario obtenerUsuario(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido"));
                u.setAvatar(rs.getString("avatar"));
                return u;
            }
        } catch (SQLException e) {
            System.out.println("[BD] Error al obtener usuario: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────
    //  UPDATE — Actualizar contraseña
    // ─────────────────────────────────────────────
    public boolean actualizarPassword(String username, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE username = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, nuevaPassword);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[BD] Error al actualizar password: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────
    //  DELETE — Eliminar usuario
    // ─────────────────────────────────────────────
    public boolean eliminarUsuario(String username) {
        String sql = "DELETE FROM usuarios WHERE username = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[BD] Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}