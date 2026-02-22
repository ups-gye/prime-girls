package battleship.client.views;

import battleship.client.NetworkClient;
import battleship.client.components.BoardPanel;
import battleship.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;

public class GameView extends JFrame implements NetworkClient.MessageListener {

    private NetworkClient client;
    private Usuario usuario;
    private String roomName;
    private String opponentName;
    private Board myBoard;
    private Board enemyVisible = new Board();

    private BoardPanel myBoardPanel;
    private BoardPanel enemyBoardPanel;
    private JLabel lblTurno;
    private JPanel statusBar;
    private boolean myTurn = false;
    private boolean gameOver = false;

    public GameView(int roomId, String roomName, String opponentName,
                    Board myBoard, NetworkClient client, Usuario usuario, boolean myTurn) {
        this.roomName = roomName != null ? roomName.replace("_", " ") : "Sala";
        this.opponentName = cleanName(opponentName);
        this.myBoard = myBoard;
        this.client = client;
        this.usuario = usuario;
        this.myTurn = myTurn; // Turno ya conocido desde el inicio

        initUI();

        // Registrar listener DESPUÉS de initUI para que lblTurno exista
        client.addListener(this);

        // Aplicar el turno inicial en la UI ahora que todo está listo
        SwingUtilities.invokeLater(this::updateTurnLabel);
    }

    private String cleanName(String name) {
        if (name == null) return "Oponente";
        String[] p = name.trim().split(" ");
        return p[p.length - 1];
    }

    private void initUI() {
        setTitle("Batalla Naval — " + roomName);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(980, 660);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!gameOver) {
                    int r = JOptionPane.showConfirmDialog(GameView.this,
                        "Abandonar la partida?", "Salir", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.YES_OPTION) {
                        client.send(new GameMessage(GameMessage.LEAVE_ROOM));
                        client.removeListener(GameView.this);
                        dispose();
                    }
                } else {
                    client.removeListener(GameView.this);
                    dispose();
                }
            }
        });

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(15, 30, 60));
        main.setBorder(new EmptyBorder(12, 15, 12, 15));

        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(20, 50, 110));
        statusBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel roomLabel = new JLabel(roomName);
        roomLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        roomLabel.setForeground(new Color(150, 200, 255));

        lblTurno = new JLabel("Cargando...", SwingConstants.CENTER);
        lblTurno.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTurno.setForeground(Color.YELLOW);

        JLabel vsLabel = new JLabel(usuario.getUsername() + "  vs  " + opponentName, SwingConstants.RIGHT);
        vsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vsLabel.setForeground(new Color(200, 220, 255));

        statusBar.add(roomLabel, BorderLayout.WEST);
        statusBar.add(lblTurno,  BorderLayout.CENTER);
        statusBar.add(vsLabel,   BorderLayout.EAST);

        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        boardsPanel.setOpaque(false);
        boardsPanel.add(buildBoardSection(false));
        boardsPanel.add(buildBoardSection(true));

        main.add(statusBar,   BorderLayout.NORTH);
        main.add(boardsPanel, BorderLayout.CENTER);
        add(main);
    }

    private JPanel buildBoardSection(boolean isEnemy) {
        JPanel section = new JPanel(new BorderLayout(5, 8));
        section.setOpaque(false);

        String titleTxt = isEnemy
            ? "Tablero de " + opponentName + "  —  ataca aqui"
            : "Tu tablero  (" + usuario.getUsername() + ")";
        JLabel title = new JLabel(titleTxt, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(isEnemy ? new Color(255, 160, 160) : new Color(150, 210, 255));

        if (isEnemy) {
            enemyBoardPanel = new BoardPanel(Board.SIZE, true);
            enemyBoardPanel.setHideShips(true);
            enemyBoardPanel.setBoard(enemyVisible);
            enemyBoardPanel.addCellListener((r, c) -> handleShoot(r, c));
            section.add(enemyBoardPanel, BorderLayout.CENTER);
        } else {
            myBoardPanel = new BoardPanel(Board.SIZE, false);
            myBoardPanel.setBoard(myBoard);
            section.add(myBoardPanel, BorderLayout.CENTER);
        }
        section.add(title, BorderLayout.NORTH);
        return section;
    }

    private void handleShoot(int row, int col) {
        if (!myTurn || gameOver) return;
        myTurn = false;
        updateTurnLabel();
        client.send(new GameMessage(GameMessage.SHOOT, row + " " + col));
    }

    @Override
    public void onMessage(GameMessage msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case GameMessage.YOUR_TURN:
                    myTurn = true; updateTurnLabel(); break;
                case GameMessage.OPPONENT_TURN:
                    myTurn = false; updateTurnLabel(); break;

                case GameMessage.SHOT_RESULT: {
                    if (enemyBoardPanel == null) break;
                    String tipo = msg.get(0);
                    int r = parseInt(msg.get(1)), c = parseInt(msg.get(2));
                    forceCell(enemyVisible, r, c, "MISS".equals(tipo) ? Board.Cell.MISS : Board.Cell.HIT);
                    enemyBoardPanel.setBoard(enemyVisible);
                    showFlash("MISS".equals(tipo) ? "Agua — " + toCoord(r,c)
                             : "SUNK".equals(tipo) ? "HUNDIDO — " + toCoord(r,c) + "!"
                             : "Impacto — " + toCoord(r,c) + "!",
                             "MISS".equals(tipo) ? Color.CYAN : new Color(255,80,80));
                    break;
                }
                case GameMessage.OPPONENT_SHOT: {
                    if (myBoardPanel == null) break;
                    String tipo = msg.get(0);
                    int r = parseInt(msg.get(1)), c = parseInt(msg.get(2));
                    forceCell(myBoard, r, c, "MISS".equals(tipo) ? Board.Cell.MISS : Board.Cell.HIT);
                    myBoardPanel.setBoard(myBoard);
                    break;
                }
                case GameMessage.GAME_OVER:
                    gameOver = true;
                    showGameOver(GameMessage.WIN.equals(msg.getPayload().trim()));
                    break;
                case GameMessage.OPPONENT_LEFT:
                    gameOver = true;
                    JOptionPane.showMessageDialog(GameView.this,
                        opponentName + " abandono. Ganaste por W.O.!",
                        "Oponente desconectado", JOptionPane.INFORMATION_MESSAGE);
                    client.removeListener(GameView.this);
                    dispose();
                    break;
            }
        });
    }

    @Override
    public void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            if (!gameOver) {
                JOptionPane.showMessageDialog(this, "Conexion perdida.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        });
    }

    private void updateTurnLabel() {
        if (lblTurno == null || statusBar == null) return;
        if (myTurn) {
            lblTurno.setText("ES TU TURNO — haz clic en el tablero enemigo");
            lblTurno.setForeground(new Color(80, 255, 120));
            statusBar.setBackground(new Color(10, 70, 30));
        } else {
            lblTurno.setText("Turno de " + opponentName + "...");
            lblTurno.setForeground(Color.YELLOW);
            statusBar.setBackground(new Color(20, 50, 110));
        }
        statusBar.repaint();
    }

    private void showFlash(String message, Color color) {
        if (lblTurno == null) return;
        lblTurno.setText(message);
        lblTurno.setForeground(color);
        new Timer(2000, e -> { updateTurnLabel(); ((Timer)e.getSource()).stop(); }).start();
    }

    private void showGameOver(boolean won) {
        String title = won ? "VICTORIA!" : "Derrota";
        String body  = won ? "Hundiste toda la flota enemiga!" : "Tu flota fue destruida.";
        if (lblTurno != null) lblTurno.setText(title);
        JOptionPane.showMessageDialog(this, body, title,
            won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        client.removeListener(this);
        dispose();
    }

    private void forceCell(Board b, int row, int col, Board.Cell state) {
        try {
            Field f = Board.class.getDeclaredField("grid");
            f.setAccessible(true);
            Board.Cell[][] grid = (Board.Cell[][]) f.get(b);
            grid[row][col] = state;
        } catch (Exception e) { b.shoot(row, col); }
    }

    private String toCoord(int row, int col) {
        String[] cols = {"A","B","C","D","E","F","G","H","I","J"};
        return col >= 0 && col < cols.length ? cols[col] + (row + 1) : "?";
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s == null ? "0" : s.trim()); } catch (Exception e) { return 0; }
    }
}
