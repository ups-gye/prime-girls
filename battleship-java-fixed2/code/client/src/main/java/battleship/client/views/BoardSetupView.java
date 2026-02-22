package battleship.client.views;

import battleship.client.NetworkClient;
import battleship.client.components.BoardPanel;
import battleship.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class BoardSetupView extends JFrame implements NetworkClient.MessageListener {

    private NetworkClient client;
    private Usuario usuario;
    private int roomId;
    private String roomName;
    private String opponentName;

    private Board board;
    private Ship selectedShipTemplate;
    private boolean horizontal = true;

    private static final Object[][] SHIP_CONFIG = {
        {Ship.Type.PORTAAVIONES, 1},
        {Ship.Type.ACORAZADO,    2},
        {Ship.Type.CRUCERO,      1},
        {Ship.Type.DESTRUCTOR,   2},
    };
    private final Map<Ship.Type, Integer> required = new LinkedHashMap<>();
    private final Map<Ship.Type, Integer> placed   = new LinkedHashMap<>();

    private BoardPanel boardPanel;
    private JPanel shipListPanel;
    private JButton btnRotate, btnReset, btnReady;
    private JLabel lblStatus, lblWaiting;

    private BoardSetupCallback callback;
    
    // Estado del juego: guardamos GAME_START y esperamos el turno
    private boolean gameStartReceived = false;
    private String pendingFirstTurn = null; // YOUR_TURN o OPPONENT_TURN

    public interface BoardSetupCallback {
        void onGameStart(int roomId, String roomName, String opponentName,
                         Board myBoard, NetworkClient client, Usuario usuario, boolean myTurn);
    }

    public BoardSetupView(int roomId, String roomName, String opponentName,
                          NetworkClient client, Usuario usuario, BoardSetupCallback callback) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.opponentName = opponentName;
        this.client = client;
        this.usuario = usuario;
        this.callback = callback;
        this.board = new Board();
        for (Object[] cfg : SHIP_CONFIG) {
            required.put((Ship.Type) cfg[0], (int) cfg[1]);
            placed.put((Ship.Type) cfg[0], 0);
        }
        client.addListener(this);
        initUI();
    }

    private void initUI() {
        setTitle("Batalla Naval — Coloca tu Flota [" + roomName + "]");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(760, 580);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                client.removeListener(BoardSetupView.this);
                client.send(new GameMessage(GameMessage.LEAVE_ROOM));
            }
        });

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(15, 30, 60));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));
        main.add(buildHeader(), BorderLayout.NORTH);

        boardPanel = new BoardPanel(Board.SIZE, true);
        boardPanel.setPreferredSize(new Dimension(370, 370));
        boardPanel.addCellListener((r, c) -> handleCellClick(r, c));
        boardPanel.addHoverListener((r, c) -> handleHover(r, c));

        shipListPanel = new JPanel();
        shipListPanel.setLayout(new BoxLayout(shipListPanel, BoxLayout.Y_AXIS));
        shipListPanel.setBackground(new Color(20, 45, 90));
        shipListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rebuildShipList();

        JScrollPane shipScroll = new JScrollPane(shipListPanel);
        shipScroll.setPreferredSize(new Dimension(200, 0));
        shipScroll.getViewport().setBackground(new Color(20, 45, 90));
        shipScroll.setBorder(null);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.add(boardPanel, BorderLayout.CENTER);
        center.add(shipScroll, BorderLayout.EAST);

        main.add(center, BorderLayout.CENTER);
        main.add(buildFooter(), BorderLayout.SOUTH);
        add(main);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel(roomName + " — Oponente: " + opponentName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(150, 210, 255));
        lblStatus = new JLabel("Selecciona un barco de la lista");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.YELLOW);
        p.add(title, BorderLayout.WEST);
        p.add(lblStatus, BorderLayout.EAST);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        p.setOpaque(false);
        btnRotate = createButton("Horizontal", new Color(60, 100, 180), Color.WHITE);
        btnReset  = createButton("Reiniciar",  new Color(150, 40, 40), Color.WHITE);
        btnReady  = createButton("LISTO PARA COMBATE", new Color(20, 140, 60), Color.WHITE);
        btnReady.setEnabled(false);
        btnReady.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWaiting = new JLabel("");
        lblWaiting.setForeground(Color.YELLOW);
        lblWaiting.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        btnRotate.addActionListener(e -> {
            horizontal = !horizontal;
            btnRotate.setText(horizontal ? "Horizontal" : "Vertical");
        });
        btnReset.addActionListener(e -> handleReset());
        btnReady.addActionListener(e -> handleReady());
        p.add(btnRotate); p.add(btnReset); p.add(btnReady); p.add(lblWaiting);
        return p;
    }

    private void rebuildShipList() {
        shipListPanel.removeAll();
        JLabel title = new JLabel("Mi Flota");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(180, 220, 255));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        shipListPanel.add(title);
        shipListPanel.add(Box.createVerticalStrut(8));
        for (Object[] cfg : SHIP_CONFIG) {
            Ship.Type type = (Ship.Type) cfg[0];
            int req = required.get(type), pl = placed.get(type);
            boolean done = pl >= req;
            JPanel row = new JPanel(new BorderLayout(4, 2));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
            row.setBackground(done ? new Color(20, 80, 40) : new Color(30, 60, 120));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(done ? new Color(60, 160, 80) : new Color(70, 110, 200)),
                new EmptyBorder(6, 8, 6, 8)));
            JLabel nameLabel = new JLabel(type.label + " (" + type.size + ")  " + pl + "/" + req);
            nameLabel.setForeground(done ? new Color(100, 255, 130) : Color.WHITE);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JPanel vis = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            vis.setOpaque(false);
            for (int i = 0; i < type.size; i++) {
                JPanel cell = new JPanel();
                cell.setPreferredSize(new Dimension(12, 12));
                cell.setBackground(done ? new Color(60, 160, 80) : new Color(70, 130, 220));
                cell.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                vis.add(cell);
            }
            row.add(nameLabel, BorderLayout.NORTH);
            row.add(vis, BorderLayout.CENTER);
            if (!done) {
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                row.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        selectedShipTemplate = new Ship(type);
                        lblStatus.setText("Colocando: " + type.label);
                    }
                });
            }
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            shipListPanel.add(row);
            shipListPanel.add(Box.createVerticalStrut(5));
        }
        shipListPanel.revalidate();
        shipListPanel.repaint();
    }

    private void handleHover(int row, int col) {
        if (selectedShipTemplate == null) return;
        List<int[]> cells = getShipCells(row, col, selectedShipTemplate.getType().size, horizontal);
        boardPanel.setHoverCells(cells, isValidPlacement(cells));
    }

    private void handleCellClick(int row, int col) {
        if (selectedShipTemplate == null) { lblStatus.setText("Selecciona un barco"); return; }
        List<int[]> cells = getShipCells(row, col, selectedShipTemplate.getType().size, horizontal);
        if (!isValidPlacement(cells)) { lblStatus.setText("Posicion invalida"); return; }
        Ship ship = new Ship(selectedShipTemplate.getType());
        for (int[] c : cells) ship.addCell(c[0], c[1]);
        board.placeShip(ship);
        placed.put(ship.getType(), placed.get(ship.getType()) + 1);
        boardPanel.setBoard(board);
        boardPanel.clearHover();
        if (placed.get(ship.getType()) < required.get(ship.getType())) {
            lblStatus.setText("Coloca el siguiente " + ship.getName());
        } else {
            selectedShipTemplate = null;
            lblStatus.setText("Selecciona el siguiente barco");
        }
        rebuildShipList();
        checkAllPlaced();
    }

    private void handleReset() {
        board = new Board();
        for (Ship.Type t : placed.keySet()) placed.put(t, 0);
        selectedShipTemplate = null;
        boardPanel.setBoard(board);
        boardPanel.clearHover();
        btnReady.setEnabled(false);
        lblStatus.setText("Selecciona un barco");
        rebuildShipList();
    }

    private void handleReady() {
        btnReady.setEnabled(false);
        btnReady.setText("Enviando...");
        lblWaiting.setText("Esperando que " + opponentName + " termine...");
        client.send(new GameMessage(GameMessage.PLACE_SHIPS, board.serializeShips()));
    }

    private void checkAllPlaced() {
        for (Ship.Type t : required.keySet())
            if (placed.get(t) < required.get(t)) return;
        btnReady.setEnabled(true);
        lblStatus.setText("Flota completa! Pulsa LISTO.");
    }

    private boolean isValidPlacement(List<int[]> cells) {
        for (int[] c : cells) {
            if (c[0] < 0 || c[0] >= Board.SIZE || c[1] < 0 || c[1] >= Board.SIZE) return false;
            if (board.getCell(c[0], c[1]) != Board.Cell.EMPTY) return false;
        }
        return !cells.isEmpty();
    }

    private List<int[]> getShipCells(int r, int c, int size, boolean horiz) {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++)
            cells.add(horiz ? new int[]{r, c + i} : new int[]{r + i, c});
        return cells;
    }

    private void launchGameIfReady() {
        // Solo lanzar GameView cuando tengamos TANTO GAME_START COMO el turno inicial
        if (gameStartReceived && pendingFirstTurn != null) {
            boolean myTurn = GameMessage.YOUR_TURN.equals(pendingFirstTurn);
            client.removeListener(BoardSetupView.this);
            Board finalBoard = board;
            setVisible(false);
            callback.onGameStart(roomId, roomName, opponentName, finalBoard, client, usuario, myTurn);
            SwingUtilities.invokeLater(() -> dispose());
        }
    }

    @Override
    public void onMessage(GameMessage msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {

                case GameMessage.SHIPS_OK:
                    lblWaiting.setText("Esperando que " + opponentName + " coloque sus barcos...");
                    break;

                case GameMessage.GAME_START:
                    gameStartReceived = true;
                    launchGameIfReady();
                    break;

                case GameMessage.YOUR_TURN:
                case GameMessage.OPPONENT_TURN:
                    // Puede llegar antes o después de GAME_START (race condition de red)
                    pendingFirstTurn = msg.getType();
                    launchGameIfReady();
                    break;

                case GameMessage.OPPONENT_LEFT:
                    client.removeListener(BoardSetupView.this);
                    setVisible(false);
                    JOptionPane.showMessageDialog(null,
                        opponentName + " abandono la sala.",
                        "Oponente desconectado", JOptionPane.WARNING_MESSAGE);
                    dispose();
                    break;
            }
        });
    }

    @Override
    public void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            JOptionPane.showMessageDialog(null, "Conexion perdida.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        });
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
