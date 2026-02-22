package battleship.monitor;

import battleship.client.NetworkClient;
import battleship.model.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitor de administración del servidor.
 * Fix (B): parseo de salas corregido para usar ; como separador de campo de sala
 *          y , como separador de campo de usuario.
 * Fix (D): botones con texto legible (color de fondo oscuro + texto blanco).
 */
public class ServerMonitor extends JFrame implements NetworkClient.MessageListener {

    private NetworkClient client;
    private final RoomPanel[] roomPanels = new RoomPanel[4];
    private JLabel lblStatus, lblConnected;
    private JButton btnConnect, btnRefresh;
    private JTextField txtHost, txtPort;
    private JPasswordField txtPassword;
    private Timer autoRefreshTimer;

    public ServerMonitor() { initUI(); }

    private void initUI() {
        setTitle("Batalla Naval — Server Monitor (Admin)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(15, 30, 60));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        main.add(buildHeader(), BorderLayout.NORTH);

        JPanel roomsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        roomsGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            roomPanels[i] = new RoomPanel(i + 1);
            roomsGrid.add(roomPanels[i]);
        }
        main.add(roomsGrid, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        lblStatus = new JLabel("Sin conectar al servidor");
        lblStatus.setForeground(Color.ORANGE);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.add(lblStatus);
        main.add(footer, BorderLayout.SOUTH);
        add(main);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setBackground(new Color(20, 45, 90));
        p.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("⚓ SERVER MONITOR — BATALLA NAVAL");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(100, 180, 255));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);

        txtHost     = makeField("localhost", 90);
        txtPort     = makeField("9090", 55);
        txtPassword = new JPasswordField("admin123");
        txtPassword.setPreferredSize(new Dimension(90, 28));
        styleField(txtPassword);

        // (D) botones con texto blanco sobre fondo oscuro — siempre legible
        btnConnect = createButton("Conectar",     new Color(40, 120, 200), Color.WHITE);
        btnRefresh = createButton("↻ Actualizar", new Color(40, 100, 50),  Color.WHITE);
        btnRefresh.setEnabled(false);

        lblConnected = new JLabel("●");
        lblConnected.setForeground(Color.RED);
        lblConnected.setFont(new Font("Segoe UI", Font.BOLD, 20));

        addLabel(row, "Servidor:"); row.add(txtHost);
        addLabel(row, ":"); row.add(txtPort);
        addLabel(row, "Pass:"); row.add(txtPassword);
        row.add(btnConnect); row.add(btnRefresh); row.add(lblConnected);

        btnConnect.addActionListener(e -> handleConnect());
        btnRefresh.addActionListener(e -> requestStatus());

        p.add(title, BorderLayout.WEST);
        p.add(row, BorderLayout.EAST);
        return p;
    }

    private void handleConnect() {
        String host = txtHost.getText().trim();
        int port;
        try { port = Integer.parseInt(txtPort.getText().trim()); }
        catch (NumberFormatException e) { showError("Puerto inválido"); return; }
        String pass = new String(txtPassword.getPassword());

        btnConnect.setEnabled(false);
        lblStatus.setText("Conectando...");
        lblStatus.setForeground(Color.YELLOW);

        new SwingWorker<Boolean, Void>() {
            NetworkClient nc;
            @Override protected Boolean doInBackground() {
                nc = new NetworkClient(host, port);
                nc.addListener(ServerMonitor.this);
                return nc.connect();
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        client = nc;
                        client.send(new GameMessage(GameMessage.MONITOR_LOGIN, pass));
                    } else {
                        lblStatus.setText("No se pudo conectar");
                        lblStatus.setForeground(Color.RED);
                        btnConnect.setEnabled(true);
                    }
                } catch (Exception ex) { btnConnect.setEnabled(true); }
            }
        }.execute();
    }

    private void requestStatus() {
        if (client != null && client.isConnected())
            client.send(new GameMessage(GameMessage.GET_STATUS));
    }

    @Override
    public void onMessage(GameMessage msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case GameMessage.LOGIN_OK:
                    lblConnected.setForeground(new Color(80, 220, 80));
                    lblStatus.setText("✓ Conectado como Monitor — actualizando cada 3s");
                    lblStatus.setForeground(new Color(80, 220, 80));
                    btnRefresh.setEnabled(true);
                    requestStatus();
                    if (autoRefreshTimer != null) autoRefreshTimer.stop();
                    autoRefreshTimer = new Timer(3000, e -> requestStatus());
                    autoRefreshTimer.start();
                    break;
                case GameMessage.LOGIN_FAIL:
                    showError("Contraseña de monitor incorrecta");
                    btnConnect.setEnabled(true);
                    lblStatus.setText("Error de autenticación");
                    lblStatus.setForeground(Color.RED);
                    break;
                case GameMessage.ROOM_LIST:
                case GameMessage.STATUS_UPDATE:
                    updateRooms(msg.getPayload());
                    break;
            }
        });
    }

    @Override
    public void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            if (autoRefreshTimer != null) autoRefreshTimer.stop();
            lblConnected.setForeground(Color.RED);
            lblStatus.setText("Desconectado del servidor");
            lblStatus.setForeground(Color.RED);
            btnConnect.setEnabled(true);
            btnRefresh.setEnabled(false);
            for (RoomPanel rp : roomPanels) rp.setEmpty();
        });
    }

    /**
     * (B) Parseo correcto de la lista de salas.
     * Salas separadas por ~. Cada sala: id;nombre;estado;j1_csv;j2_csv;turnoId;d1;a1;h1;d2;a2;h2
     */
    private void updateRooms(String raw) {
        List<String> parts = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String s : raw.split("~")) {
                s = s.trim();
                if (!s.isEmpty()) parts.add(s);
            }
        }
        for (int i = 0; i < 4; i++) {
            if (i < parts.size()) {
                try { roomPanels[i].updateFromRaw(parts.get(i)); }
                catch (Exception e) {
                    System.err.println("[Monitor] Error parseando sala " + i + ": " + e.getMessage());
                    roomPanels[i].setEmpty();
                }
            } else {
                roomPanels[i].setEmpty();
            }
        }
    }

    // ── Panel de sala (reproduce Figura 1 del PDF) ────────────────────
    class RoomPanel extends JPanel {
        int slot;
        JLabel lblEstado, lblTurno;
        PlayerStatsPanel statsJ1, statsJ2;

        RoomPanel(int slot) {
            this.slot = slot;
            setBackground(new Color(20, 45, 100));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 100, 200), 2),
                new EmptyBorder(8, 10, 8, 10)));
            setLayout(new BorderLayout(5, 5));

            JLabel lblName = new JLabel("SALA " + slot, SwingConstants.CENTER);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblName.setForeground(new Color(180, 220, 255));

            lblEstado = new JLabel("", SwingConstants.CENTER);
            lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblTurno  = new JLabel("", SwingConstants.CENTER);
            lblTurno.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblTurno.setForeground(new Color(100, 255, 100));

            JPanel topRow = new JPanel(new GridLayout(3, 1));
            topRow.setOpaque(false);
            topRow.add(lblName); topRow.add(lblEstado); topRow.add(lblTurno);

            JPanel playersRow = new JPanel(new GridLayout(1, 2, 10, 0));
            playersRow.setOpaque(false);
            statsJ1 = new PlayerStatsPanel();
            statsJ2 = new PlayerStatsPanel();
            playersRow.add(statsJ1); playersRow.add(statsJ2);

            add(topRow, BorderLayout.NORTH);
            add(playersRow, BorderLayout.CENTER);
            setEmpty();
        }

        void setEmpty() {
            lblEstado.setText("Vacía");
            lblEstado.setForeground(new Color(120, 140, 180));
            lblTurno.setText("");
            statsJ1.setEmpty(); statsJ2.setEmpty();
            setBackground(new Color(20, 35, 70));
        }

        void updateFromRaw(String raw) {
            // Formato: id;nombre;estado;j1_csv;j2_csv;turnoId;d1;a1;h1;d2;a2;h2
            String[] p = raw.split(";", 12);
            if (p.length < 3) { setEmpty(); return; }

            GameRoom.Estado estado = GameRoom.Estado.valueOf(p[2].trim());
            Usuario j1 = p.length > 3 ? Usuario.fromPublicString(p[3].trim()) : null;
            Usuario j2 = p.length > 4 ? Usuario.fromPublicString(p[4].trim()) : null;
            int turnoId = p.length > 5 ? intOrZero(p[5]) : 0;

            String estadoTxt; Color estadoColor;
            switch (estado) {
                case ESPERANDO:    estadoTxt = "⏳ Esperando oponente"; estadoColor = new Color(255, 200, 50);  setBackground(new Color(30, 50, 90));  break;
                case CONFIGURANDO: estadoTxt = "⚙ Colocando barcos";   estadoColor = new Color(100, 180, 255); setBackground(new Color(25, 50, 100)); break;
                case EN_JUEGO:     estadoTxt = "⚔ En Juego";           estadoColor = new Color(80, 220, 80);   setBackground(new Color(20, 60, 35));  break;
                default:           estadoTxt = "✓ Finalizada";         estadoColor = Color.GRAY;               setBackground(new Color(30, 30, 50));  break;
            }
            lblEstado.setText(estadoTxt);
            lblEstado.setForeground(estadoColor);

            if (estado == GameRoom.Estado.EN_JUEGO && j1 != null && j2 != null) {
                String turn = turnoId == j1.getId() ? j1.getUsername() : j2.getUsername();
                lblTurno.setText("Turno de " + turn);
            } else { lblTurno.setText(""); }

            int d1 = intAtOrZero(p, 6),  a1 = intAtOrZero(p, 7),  h1 = intAtOrZero(p, 8);
            int d2 = intAtOrZero(p, 9),  a2 = intAtOrZero(p, 10), h2 = intAtOrZero(p, 11);

            if (j1 != null) statsJ1.update(j1.getUsername(), d1, a1, h1);
            else statsJ1.setEmpty();
            if (j2 != null) statsJ2.update(j2.getUsername(), d2, a2, h2);
            else statsJ2.setEmpty();
        }
    }

    class PlayerStatsPanel extends JPanel {
        JLabel lblName, lblDisparos, lblAciertos, lblHundidos;

        PlayerStatsPanel() {
            setBackground(new Color(25, 55, 110));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 90, 160)),
                new EmptyBorder(6, 8, 6, 8)));
            setLayout(new GridLayout(4, 1, 0, 2));
            lblName     = stat("—", true);
            lblDisparos = stat("Disparos totales: —", false);
            lblAciertos = stat("Aciertos: —", false);
            lblHundidos = stat("Barcos hundidos: —", false);
            add(lblName); add(lblDisparos); add(lblAciertos); add(lblHundidos);
        }
        void setEmpty() {
            lblName.setText("—"); lblDisparos.setText("Disparos totales: —");
            lblAciertos.setText("Aciertos: —"); lblHundidos.setText("Barcos hundidos: —");
            setBackground(new Color(20, 35, 70));
        }
        void update(String username, int d, int a, int h) {
            lblName.setText(username);
            lblDisparos.setText("Disparos totales: " + d);
            lblAciertos.setText("Aciertos: " + a);
            lblHundidos.setText("Barcos hundidos: " + h + "/5");
            setBackground(new Color(25, 55, 110));
        }
        private JLabel stat(String txt, boolean bold) {
            JLabel l = new JLabel(txt, SwingConstants.LEFT);
            l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 11));
            l.setForeground(new Color(180, 210, 255));
            return l;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JTextField makeField(String text, int width) {
        JTextField f = new JTextField(text);
        f.setPreferredSize(new Dimension(width, 28));
        styleField(f); return f;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(40, 70, 140)); f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 120, 200)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void addLabel(JPanel p, String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.WHITE);
        p.add(l);
    }

    private int intOrZero(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
    private int intAtOrZero(String[] arr, int idx) {
        if (idx >= arr.length) return 0;
        return intOrZero(arr[idx]);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ServerMonitor().setVisible(true));
    }
}
