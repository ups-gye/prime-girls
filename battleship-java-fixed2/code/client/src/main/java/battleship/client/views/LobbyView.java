package battleship.client.views;

import battleship.client.NetworkClient;
import battleship.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class LobbyView extends JFrame implements NetworkClient.MessageListener {

    private NetworkClient client;
    private Usuario usuario;
    private JLabel lblWins, lblLosses, lblPoints;
    private JPanel roomsPanel;
    private JButton btnCreate, btnRefresh, btnLogout;
    private JLabel lblStatus;
    private LobbyCallback callback;

    public interface LobbyCallback {
        void onJoinRoom(int roomId, String roomName, String opponentName, NetworkClient c, Usuario u);
        void onLogout();
    }

    public LobbyView(Usuario usuario, NetworkClient client, LobbyCallback callback) {
        this.usuario = usuario;
        this.client = client;
        this.callback = callback;
        client.addListener(this);
        initUI();
        refreshRooms();
    }

    private void initUI() {
        setTitle("Batalla Naval — Sala de Espera (" + usuario.getUsername() + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(720, 560);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { handleLogout(); }
        });

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(15, 30, 60));
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("⚓ CENTRO DE MANDO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(100, 180, 255));
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        lblWins   = makeStatLabel("🏆 " + usuario.getPartidasGanadas(),  new Color(80, 200, 100));
        lblLosses = makeStatLabel("💀 " + usuario.getPartidasPerdidas(), new Color(220, 80, 80));
        lblPoints = makeStatLabel("⭐ " + usuario.getPuntosTotales(),    new Color(255, 200, 50));
        statsPanel.add(lblWins); statsPanel.add(lblLosses); statsPanel.add(lblPoints);
        header.add(title, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        // Perfil
        JPanel profileBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        profileBar.setBackground(new Color(25, 50, 100));
        profileBar.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel lblUser = new JLabel("Capitán: " + usuario.getNombre() + " " + usuario.getApellido()
                + "  (@" + usuario.getUsername() + ")");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        profileBar.add(lblUser);

        // Toolbar
        JPanel roomToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomToolbar.setOpaque(false);
        JLabel roomTitle = new JLabel("Salas de Batalla (máx. 4)");
        roomTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        roomTitle.setForeground(new Color(180, 220, 255));
        btnCreate  = createButton("+ Crear Sala", new Color(30, 100, 200), Color.WHITE);
        btnRefresh = createButton("↻ Actualizar", new Color(40, 100, 50),  Color.WHITE);
        btnLogout  = createButton("Salir",         new Color(150, 30, 30), Color.WHITE);
        lblStatus  = new JLabel("");
        lblStatus.setForeground(Color.YELLOW);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        roomToolbar.add(roomTitle);
        roomToolbar.add(Box.createHorizontalStrut(10));
        roomToolbar.add(btnCreate); roomToolbar.add(btnRefresh); roomToolbar.add(btnLogout);
        roomToolbar.add(lblStatus);

        // Grid salas
        roomsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        roomsPanel.setBackground(new Color(20, 40, 80));
        roomsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        renderEmptySlots(new ArrayList<>());

        JPanel roomsSection = new JPanel(new BorderLayout(5, 5));
        roomsSection.setOpaque(false);
        roomsSection.add(roomToolbar, BorderLayout.NORTH);
        roomsSection.add(new JScrollPane(roomsPanel), BorderLayout.CENTER);

        JPanel topBlock = new JPanel(new BorderLayout(5, 5));
        topBlock.setOpaque(false);
        topBlock.add(header, BorderLayout.NORTH);
        topBlock.add(profileBar, BorderLayout.CENTER);

        main.add(topBlock, BorderLayout.NORTH);
        main.add(roomsSection, BorderLayout.CENTER);
        add(main);

        btnCreate.addActionListener(e -> client.send(new GameMessage(GameMessage.CREATE_ROOM)));
        btnRefresh.addActionListener(e -> refreshRooms());
        btnLogout.addActionListener(e -> handleLogout());
    }

    private void refreshRooms() {
        lblStatus.setText("Actualizando...");
        client.send(new GameMessage(GameMessage.GET_ROOMS));
    }

    private void handleLogout() {
        client.removeListener(this);
        client.send(new GameMessage(GameMessage.LEAVE_ROOM));
        client.disconnect();
        dispose();
        callback.onLogout();
    }

    private void renderEmptySlots(List<GameRoom> rooms) {
        roomsPanel.removeAll();
        for (int i = 0; i < 4; i++) {
            GameRoom room = i < rooms.size() ? rooms.get(i) : null;
            roomsPanel.add(buildRoomCard(room, i + 1));
        }
        roomsPanel.revalidate();
        roomsPanel.repaint();
    }

    private JPanel buildRoomCard(GameRoom room, int slot) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(20, 50, 100));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 100, 180)),
            new EmptyBorder(10, 12, 10, 12)));

        if (room == null) {
            JLabel empty = new JLabel("Sala " + slot + " — Vacía", SwingConstants.CENTER);
            empty.setForeground(new Color(100, 130, 180));
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            card.add(empty, BorderLayout.CENTER);
            return card;
        }

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel nameLabel = new JLabel(room.getNombre());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(new Color(150, 200, 255));

        String estadoTxt; Color estadoColor;
        switch (room.getEstado()) {
            case ESPERANDO:    estadoTxt = "⏳ Esperando";    estadoColor = new Color(255, 200, 50);  break;
            case CONFIGURANDO: estadoTxt = "⚙ Configurando"; estadoColor = new Color(100, 180, 255); break;
            case EN_JUEGO:     estadoTxt = "⚔ En Juego";     estadoColor = new Color(80, 220, 80);   break;
            default:           estadoTxt = "✓ Finalizada";   estadoColor = Color.GRAY;               break;
        }
        JLabel estadoLabel = new JLabel(estadoTxt);
        estadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        estadoLabel.setForeground(estadoColor);
        topRow.add(nameLabel, BorderLayout.WEST);
        topRow.add(estadoLabel, BorderLayout.EAST);

        String j1 = room.getJugador1() != null ? room.getJugador1().getUsername() : "?";
        String j2 = room.getJugador2() != null ? room.getJugador2().getUsername() : "Esperando...";
        JLabel playersLabel = new JLabel(j1 + "  vs  " + j2, SwingConstants.CENTER);
        playersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        playersLabel.setForeground(new Color(200, 220, 255));

        card.add(topRow, BorderLayout.NORTH);
        card.add(playersLabel, BorderLayout.CENTER);

        boolean puedoUnirme = room.getEstado() == GameRoom.Estado.ESPERANDO
                && !room.tieneJugador(usuario.getId());
        if (puedoUnirme) {
            JButton joinBtn = createButton("⚔ Unirse", new Color(30, 130, 60), Color.WHITE);
            joinBtn.addActionListener(e -> {
                client.send(new GameMessage(GameMessage.JOIN_ROOM, String.valueOf(room.getId())));
                btnCreate.setEnabled(false);
            });
            card.add(joinBtn, BorderLayout.SOUTH);
        }
        return card;
    }

    // ── MessageListener ───────────────────────────────────────────────

    @Override
    public void onMessage(GameMessage msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {

                case GameMessage.ROOM_LIST:
                    List<GameRoom> rooms = parseRoomList(msg.getPayload());
                    renderEmptySlots(rooms);
                    lblStatus.setText("");
                    break;

                case GameMessage.ROOM_CREATED:
                    lblStatus.setText("Sala creada. Esperando oponente...");
                    break;

                case GameMessage.ROOM_JOINED:
                    // Payload: "roomId roomName opponentUsername"
                    // roomName puede ser "Sala_1" (sin espacios), opponentUsername sin espacios
                    // Ejemplo: "3 Sala_3 jugador1"
                    handleRoomJoined(msg.getPayload());
                    break;

                case GameMessage.ROOM_FULL:
                case GameMessage.SERVER_FULL:
                    showError(msg.getPayload());
                    btnCreate.setEnabled(true);
                    lblStatus.setText("");
                    break;

                case GameMessage.ERROR:
                    lblStatus.setText("Error: " + msg.getPayload());
                    break;
            }
        });
    }

    private void handleRoomJoined(String payload) {
        // Payload siempre es "roomId roomName opponentName"
        // roomName es como "Sala_3" (sin espacios internos)
        // opponentName es username sin espacios
        try {
            String[] parts = payload.trim().split(" ", 3);
            int roomId = Integer.parseInt(parts[0].trim());
            String roomName    = parts.length > 1 ? parts[1].trim().replace("_", " ") : "Sala";
            String opponentName = parts.length > 2 ? parts[2].trim() : "Oponente";

            // CRÍTICO: setVisible(false) NO dispara windowClosing → no envía LEAVE_ROOM
            client.removeListener(LobbyView.this);
            setVisible(false);
            callback.onJoinRoom(roomId, roomName, opponentName, client, usuario);
            SwingUtilities.invokeLater(() -> dispose());
        } catch (Exception e) {
            System.err.println("[LobbyView] Error parseando ROOM_JOINED: '" + payload + "' → " + e.getMessage());
            lblStatus.setText("Error al unirse a la sala");
            btnCreate.setEnabled(true);
        }
    }

    @Override
    public void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            showError("Conexión perdida con el servidor.");
            dispose();
            callback.onLogout();
        });
    }

    // ── Parseo de salas ───────────────────────────────────────────────

    private List<GameRoom> parseRoomList(String raw) {
        List<GameRoom> rooms = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return rooms;
        for (String s : raw.split("~")) {
            s = s.trim();
            if (s.isEmpty()) continue;
            try {
                GameRoom r = parseRoom(s);
                if (r != null) rooms.add(r);
            } catch (Exception e) {
                System.err.println("[LobbyView] parseRoom error: " + e.getMessage() + " raw='" + s + "'");
            }
        }
        return rooms;
    }

    private GameRoom parseRoom(String raw) {
        // Formato: id;nombre;estado;j1_csv;j2_csv;turnoId;d1;a1;h1;d2;a2;h2
        String[] p = raw.split(";", 12);
        if (p.length < 3) throw new IllegalArgumentException("Muy pocos campos: " + raw);
        int id = Integer.parseInt(p[0].trim());
        GameRoom.Estado estado = GameRoom.Estado.valueOf(p[2].trim());
        Usuario j1 = p.length > 3 ? Usuario.fromPublicString(p[3].trim()) : null;
        Usuario j2 = p.length > 4 ? Usuario.fromPublicString(p[4].trim()) : null;
        GameRoom room = new GameRoom(id, j1);
        if (j2 != null) room.setJugador2(j2);
        room.setEstado(estado);
        if (p.length > 5) {
            try { room.setTurnoActualId(Integer.parseInt(p[5].trim())); } catch (Exception ignored) {}
        }
        return room;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel makeStatLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(color);
        return l;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
