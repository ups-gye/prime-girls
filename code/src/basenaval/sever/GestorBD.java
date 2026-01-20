package basenaval.sever; // Asegúrate que coincida con tu paquete (ej. .sever o .server)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GestorBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/battleship_db";
    private static final String USER = "postgres";
    private static final String PASS = "F272jos46"; 

    public Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Error BD: " + e.getMessage());
            return null;
        }
    }
    public boolean validarUsuario(String usuario, String password) {
        String sql = "SELECT id FROM usuarios WHERE username = ? AND password = ?";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, usuario);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { return false; }
    }
    public boolean registrarUsuario(String usuario, String password, String nombre, String apellido, String avatar) {
        String sql = "INSERT INTO usuarios (username, password, nombre, apellido, avatar) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, usuario);
            ps.setString(2, password);
            ps.setString(3, nombre);
            ps.setString(4, apellido);
            ps.setString(5, avatar);
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error registro: " + e.getMessage());
            return false;
        }
    }
}