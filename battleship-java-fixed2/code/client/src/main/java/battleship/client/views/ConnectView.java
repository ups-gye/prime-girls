package battleship.client.views;

import battleship.client.NetworkClient;
import battleship.model.GameMessage;
import battleship.model.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Vista de Login y Registro.
 * Fixes aplicados:
 *  - Ventana más grande (520x600) para que no se corten los campos de registro
 *  - Botones con texto legible (oscuro sobre fondo claro / blanco sobre fondo oscuro con contraste)
 *  - Parseo de LOGIN_OK con nuevo protocolo (espacio como separador)
 */
public class ConnectView extends JFrame implements NetworkClient.MessageListener {

    private NetworkClient client;
    private JTextField txtHost, txtPort;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtNombre, txtApellido;
    private JTextField txtRegUser;
    private JPasswordField txtRegPass;
    private JComboBox<String> cmbAvatar;
    private JButton btnLogin, btnRegister, btnConnect;
    private JLabel lblStatus;
    private JTabbedPane tabs;

    private LoginCallback loginCallback;

    public interface LoginCallback {
        void onLoginSuccess(Usuario usuario, NetworkClient client);
    }

    public ConnectView(LoginCallback callback) {
        this.loginCallback = callback;
        initUI();
    }

    private void initUI() {
        setTitle("Batalla Naval — Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 620);   // (E) ventana más grande para evitar corte en registro
        setMinimumSize(new Dimension(460, 540));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(15, 30, 60));
        main.setBorder(new EmptyBorder(20, 30, 20, 30));

        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("⚓ BATALLA NAVAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(100, 180, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("CENTRO DE MANDO", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(150, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(12));

        // ── Fila de conexión al servidor ──────────────────────────────
        JPanel connectRow = new JPanel(new GridBagLayout());
        connectRow.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 3, 0, 3);
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtHost = new JTextField("localhost");
        txtPort = new JTextField("9090");
        styleTextField(txtHost);
        styleTextField(txtPort);
        txtPort.setPreferredSize(new Dimension(60, 30));

        btnConnect = createButton("Conectar", new Color(40, 120, 200), Color.WHITE);

        lblStatus = new JLabel("Sin conectar");
        lblStatus.setForeground(Color.ORANGE);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));

        gc.gridx = 0; gc.weightx = 1; connectRow.add(txtHost, gc);
        gc.gridx = 1; gc.weightx = 0; connectRow.add(txtPort, gc);
        gc.gridx = 2; connectRow.add(btnConnect, gc);
        gc.gridx = 3; connectRow.add(lblStatus, gc);

        header.add(connectRow);

        // ── Tabs ──────────────────────────────────────────────────────
        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(new Color(20, 45, 90));
        tabs.setForeground(Color.WHITE);
        tabs.addTab("Iniciar Sesión", buildLoginPanel());
        tabs.addTab("Registrarse",    buildRegisterPanel());

        main.add(header, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        add(main);

        // ── Eventos ───────────────────────────────────────────────────
        btnConnect.addActionListener(e -> handleConnect());
        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> handleRegister());
        setLoginEnabled(false);
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(25, 50, 100));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        g.gridx = 0; g.weightx = 1;

        txtUsername = new JTextField(); styleTextField(txtUsername);
        txtPassword = new JPasswordField(); styleTextField(txtPassword);
        btnLogin = createButton("ENTRAR AL COMBATE", new Color(30, 100, 200), Color.WHITE);

        g.gridy = 0; p.add(makeLabel("Usuario"), g);
        g.gridy = 1; p.add(txtUsername, g);
        g.gridy = 2; p.add(makeLabel("Contraseña"), g);
        g.gridy = 3; p.add(txtPassword, g);
        g.gridy = 4; g.insets = new Insets(16, 0, 6, 0); p.add(btnLogin, g);
        return p;
    }

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(25, 50, 100));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.gridx = 0; g.weightx = 1;

        txtRegUser = new JTextField();  styleTextField(txtRegUser);
        txtRegPass = new JPasswordField(); styleTextField(txtRegPass);
        txtNombre   = new JTextField(); styleTextField(txtNombre);
        txtApellido = new JTextField(); styleTextField(txtApellido);
        cmbAvatar = new JComboBox<>(new String[]{
            "captain-1","captain-2","captain-3","captain-4","captain-5","captain-6"
        });
        cmbAvatar.setBackground(new Color(40, 80, 150));
        cmbAvatar.setForeground(Color.WHITE);
        btnRegister = createButton("GUARDAR Y ZARPAR", new Color(20, 140, 80), Color.WHITE);

        g.gridy = 0; p.add(makeLabel("Usuario"),    g);
        g.gridy = 1; p.add(txtRegUser,              g);
        g.gridy = 2; p.add(makeLabel("Contraseña"), g);
        g.gridy = 3; p.add(txtRegPass,              g);
        g.gridy = 4; p.add(makeLabel("Nombre"),     g);
        g.gridy = 5; p.add(txtNombre,               g);
        g.gridy = 6; p.add(makeLabel("Apellido"),   g);
        g.gridy = 7; p.add(txtApellido,             g);
        g.gridy = 8; p.add(makeLabel("Avatar"),     g);
        g.gridy = 9; p.add(cmbAvatar,               g);
        g.gridy = 10; g.insets = new Insets(12, 0, 4, 0); p.add(btnRegister, g);
        return p;
    }

    // ── Handlers ──────────────────────────────────────────────────────

    private void handleConnect() {
        String host = txtHost.getText().trim();
        int port;
        try { port = Integer.parseInt(txtPort.getText().trim()); }
        catch (NumberFormatException e) { showError("Puerto inválido"); return; }

        btnConnect.setEnabled(false);
        lblStatus.setText("Conectando...");
        lblStatus.setForeground(Color.YELLOW);

        new SwingWorker<Boolean, Void>() {
            NetworkClient nc;
            @Override protected Boolean doInBackground() {
                nc = new NetworkClient(host, port);
                nc.addListener(ConnectView.this);
                return nc.connect();
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        client = nc;
                        lblStatus.setText("✓ Conectado");
                        lblStatus.setForeground(new Color(80, 200, 80));
                        setLoginEnabled(true);
                    } else {
                        lblStatus.setText("✗ Error");
                        lblStatus.setForeground(Color.RED);
                        btnConnect.setEnabled(true);
                    }
                } catch (Exception ex) { btnConnect.setEnabled(true); }
            }
        }.execute();
    }

    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) { showError("Completa todos los campos"); return; }
        setLoginEnabled(false);
        // LOGIN username password
        client.send(new GameMessage(GameMessage.LOGIN, user + " " + pass));
    }

    private void handleRegister() {
        String user     = txtRegUser.getText().trim();
        String pass     = new String(txtRegPass.getPassword());
        String nombre   = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String avatar   = (String) cmbAvatar.getSelectedItem();

        if (user.isEmpty() || pass.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
            showError("Completa todos los campos"); return;
        }
        if (pass.length() < 4) { showError("Contraseña mínimo 4 caracteres"); return; }
        setLoginEnabled(false);
        // REGISTER username password nombre apellido avatar
        client.send(new GameMessage(GameMessage.REGISTER, user + " " + pass + " " + nombre + " " + apellido + " " + avatar));
    }

    // ── MessageListener ───────────────────────────────────────────────

    @Override
    public void onMessage(GameMessage msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case GameMessage.LOGIN_OK:
                    // payload: "ganadas perdidas datos_usuario_csv"
                    try {
                        String[] parts = msg.getPayload().split(" ", 3);
                        if (parts.length < 3) throw new Exception("Payload corto: " + msg.getPayload());
                        Usuario u = Usuario.fromPublicString(parts[2]);
                        if (u == null) throw new Exception("Usuario null desde: " + parts[2]);
                        dispose();
                        loginCallback.onLoginSuccess(u, client);
                    } catch (Exception e) {
                        System.err.println("[LOGIN_OK] Error parseando usuario: " + e.getMessage());
                        showError("Error al procesar login. Revisa la consola.");
                        setLoginEnabled(true);
                    }
                    break;
                case GameMessage.LOGIN_FAIL:
                    showError(msg.getPayload());
                    setLoginEnabled(true);
                    break;
                case GameMessage.REGISTER_OK:
                    JOptionPane.showMessageDialog(this, "¡Registro exitoso! Ahora inicia sesión.",
                        "Registro", JOptionPane.INFORMATION_MESSAGE);
                    tabs.setSelectedIndex(0);
                    setLoginEnabled(true);
                    break;
                case GameMessage.REGISTER_FAIL:
                    showError(msg.getPayload());
                    setLoginEnabled(true);
                    break;
            }
        });
    }

    @Override
    public void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            lblStatus.setText("✗ Desconectado");
            lblStatus.setForeground(Color.RED);
            setLoginEnabled(false);
            btnConnect.setEnabled(true);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void setLoginEnabled(boolean en) {
        if (btnLogin   != null) btnLogin.setEnabled(en);
        if (btnRegister!= null) btnRegister.setEnabled(en);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleTextField(JTextField f) {
        f.setBackground(new Color(40, 80, 150));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 130, 220)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 34));
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(180, 210, 255));
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return l;
    }

    /** (D) Botones con contraste garantizado: texto claro sobre fondo oscuro */
    private JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }
}
