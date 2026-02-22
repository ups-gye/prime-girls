package battleship.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 100)
    private String avatar;

    @Column(name = "partidas_ganadas")
    private int partidasGanadas;

    @Column(name = "partidas_perdidas")
    private int partidasPerdidas;

    @Column(name = "puntos_totales")
    private int puntosTotales;

    public Usuario() {}

    public Usuario(String username, String password, String nombre, String apellido, String avatar) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.avatar = (avatar != null && !avatar.isEmpty()) ? avatar : "captain-1";
    }

    // ── Serialización en red ─────────────────────────────────────────
    // Formato: "id,username,nombre,apellido,avatar,ganadas,perdidas,puntos"
    // Usamos coma como separador interno de usuario.
    // La sala usa ; como separador de campos de sala.
    // El protocolo usa espacio como separador tipo/payload.
    // Nunca se mezclan.

    public String toPublicString() {
        String safeNombre   = safe(nombre);
        String safeApellido = safe(apellido);
        String safeAvatar   = safe(avatar);
        return id + "," + username + "," + safeNombre + "," + safeApellido + ","
                + safeAvatar + "," + partidasGanadas + "," + partidasPerdidas + "," + puntosTotales;
    }

    /** Reemplaza comas y punto-y-comas en strings del usuario para no romper el parseo */
    private static String safe(String s) {
        if (s == null) return "_";
        return s.replace(",", ".").replace(";", ".").replace(" ", "_");
    }

    public static Usuario fromPublicString(String s) {
        if (s == null || s.isEmpty() || "null".equals(s)) return null;
        try {
            String[] p = s.split(",", -1);
            if (p.length < 8) {
                System.err.println("[Usuario] fromPublicString: campos insuficientes en: " + s);
                return null;
            }
            Usuario u = new Usuario();
            u.id               = Integer.parseInt(p[0].trim());
            u.username         = p[1].trim();
            u.nombre           = p[2].trim().replace("_", " ");
            u.apellido         = p[3].trim().replace("_", " ");
            u.avatar           = p[4].trim();
            u.partidasGanadas  = Integer.parseInt(p[5].trim());
            u.partidasPerdidas = Integer.parseInt(p[6].trim());
            u.puntosTotales    = Integer.parseInt(p[7].trim());
            if (u.avatar.isEmpty() || u.avatar.equals("_")) u.avatar = "captain-1";
            return u;
        } catch (Exception e) {
            System.err.println("[Usuario] fromPublicString error con: '" + s + "' → " + e.getMessage());
            return null;
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getAvatar() { return avatar != null ? avatar : "captain-1"; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getPartidasGanadas() { return partidasGanadas; }
    public void setPartidasGanadas(int n) { this.partidasGanadas = n; }
    public int getPartidasPerdidas() { return partidasPerdidas; }
    public void setPartidasPerdidas(int n) { this.partidasPerdidas = n; }
    public int getPuntosTotales() { return puntosTotales; }
    public void setPuntosTotales(int n) { this.puntosTotales = n; }

    @Override
    public String toString() { return "Usuario{" + username + "}"; }
}
