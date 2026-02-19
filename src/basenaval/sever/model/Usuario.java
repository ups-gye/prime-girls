package basenaval.sever.model;


public class Usuario {
    private int id;
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String avatar;

    public Usuario() {}

    public Usuario(String username, String password, String nombre, String apellido, String avatar) {
        this.username = username;
        this.password = password;
        this.nombre   = nombre;
        this.apellido = apellido;
        this.avatar   = avatar;
    }

    // Getters
    public int    getId()       { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getAvatar()   { return avatar; }

    // Setters
    public void setId(int id)             { this.id       = id; }
    public void setUsername(String u)     { this.username = u; }
    public void setPassword(String p)     { this.password = p; }
    public void setNombre(String n)       { this.nombre   = n; }
    public void setApellido(String a)     { this.apellido = a; }
    public void setAvatar(String av)      { this.avatar   = av; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", username='" + username + "', nombre='" + nombre + " " + apellido + "'}";
    }
}